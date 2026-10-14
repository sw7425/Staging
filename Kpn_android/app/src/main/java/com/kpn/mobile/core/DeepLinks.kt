package com.kpn.mobile.core

import java.net.URI
import java.net.URLDecoder

data class NotificationDeepLink(val route:String,val event:String?=null,val notificationId:Int?=null)
object DeepLinkParser {
    fun fromPayload(data:Map<String,String?>)=parse(data["deep_link"],data["link"] ?: data["route"],data["event"],data["notification_id"],data["target_type"],data["target_id"])

    fun parse(deepLink:String?,link:String?,event:String?,notificationId:String?,targetType:String?=null,targetId:String?=null):NotificationDeepLink {
        val raw=(deepLink ?: link).orEmpty().trim()
        val uri=runCatching{URI(raw)}.getOrNull()
        val safe=uri!=null && (uri.scheme==null && uri.host==null && !raw.startsWith("//") || uri.scheme=="https"&&uri.host in setOf("kpn.com.ng","www.kpn.com.ng") || uri.scheme=="kpn"&&uri.host=="open")
        val requestedRoute=if(safe&&uri?.scheme=="kpn")uri.rawQuery?.split('&')?.firstOrNull{it.startsWith("route=")}?.substringAfter('=')?.let{runCatching{URLDecoder.decode(it,"UTF-8")}.getOrNull()} else null
        val allowedRoute=requestedRoute?.takeIf{it in setOf("home","dashboard","notifications","reports","received_announcements","community_report_review","my_articles","profile","news_member","opportunities_member")||it.matches(Regex("event/[0-9]{1,10}"))||it.matches(Regex("article/[a-zA-Z0-9_-]{1,120}"))}
        val path=if(safe)(uri?.path ?: raw).trim('/').removePrefix("api/v1/") else ""
        val slug=path.split('/').getOrNull(1)?.takeIf{it.matches(Regex("[a-zA-Z0-9_-]{1,120}"))}
        val route=when {
            !safe->"notifications"
            allowedRoute!=null->allowedRoute
            path=="account/dashboard"||path=="dashboard"||event=="MEMBER_APPROVED"->"dashboard"
            path=="newsroom"->"news_member"
            path=="opportunities"->"opportunities_member"
            (path.startsWith("newsroom/")||path.startsWith("article/"))&&slug!=null->"article/$slug"
            path.startsWith("events/")&&slug?.toIntOrNull()!=null->"event/$slug"
            targetType=="event"&&targetId?.toIntOrNull()!=null->"event/$targetId"
            targetType=="report"||path.contains("reports")->"reports"
            targetType=="announcement"->"received_announcements"
            targetType=="community_report"->"community_report_review"
            targetType=="article"->"my_articles"
            else->"notifications"
        }
        return NotificationDeepLink(route,event?.take(80),notificationId?.toIntOrNull())
    }
}
