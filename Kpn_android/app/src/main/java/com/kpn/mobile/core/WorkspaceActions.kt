package com.kpn.mobile.core

import com.kpn.mobile.data.UserDto

data class WorkspaceAction(val title:String,val description:String,val route:String,val capabilities:Set<String>,val section:String)
object WorkspaceActions {
    val all=listOf(
        WorkspaceAction("Review applicants","Membership approval within your jurisdiction","members",setOf("REVIEW_APPLICANTS"),"Membership"),
        WorkspaceAction("Mobilization contacts","Filter and export contacts within your authorized scope","mobilization",emptySet(),"Membership"),
        WorkspaceAction("Member directory","View authorized members and profiles","members",setOf("VIEW_MEMBERS"),"Membership"),
        WorkspaceAction("Submit report","Submit through the constitutional reporting hierarchy","submit_report",setOf("SUBMIT_REPORTS"),"Reports"),
        WorkspaceAction("Review reports","Review, flag, approve and escalate authorized reports","reports",setOf("REVIEW_REPORTS","OVERSIGHT_REPORTS"),"Reports"),
        WorkspaceAction("My reports","Track your submitted reports","reports",setOf("SUBMIT_REPORTS"),"Reports"),
        WorkspaceAction("Draft an article","Create and submit a newsroom draft","compose_article",setOf("WRITE_ARTICLES"),"Newsroom"),
        WorkspaceAction("My articles","Edit drafts and track editorial decisions","my_articles",setOf("WRITE_ARTICLES"),"Newsroom"),
        WorkspaceAction("Editorial review","Review the real pending article queue","editorial",setOf("REVIEW_ARTICLES"),"Newsroom"),
        WorkspaceAction("Upload media","Submit a photo or video to the media pipeline","media_upload",setOf("UPLOAD_MEDIA"),"Newsroom"),
        WorkspaceAction("Review media","Approve or reject submitted media","media_review",setOf("REVIEW_MEDIA"),"Newsroom"),
        WorkspaceAction("Community reports","Review submitted community information","community_report_review",setOf("REVIEW_COMMUNITY_REPORTS"),"Newsroom"),
        WorkspaceAction("Submit community report","Report an incident using your verified identity","community_report",setOf("SUBMIT_COMMUNITY_REPORT"),"Newsroom"),
        WorkspaceAction("Trusted reporters","Manage reporter accreditation","trusted_reporters",setOf("MANAGE_REPORTERS"),"Newsroom"),
        WorkspaceAction("Create event","Create an event with venue and schedule","create_event",setOf("MANAGE_EVENTS"),"Events"),
        WorkspaceAction("Events & meetings","Calendar, details and published minutes","events",setOf("VIEW_EVENTS"),"Events"),
        WorkspaceAction("Ward meetings","Your ward meeting logbook and minutes","ward_meetings",setOf("VIEW_WARD_MEETINGS"),"Events"),
        WorkspaceAction("Record ward meeting","Create a ward meeting record","ward_meeting_form",setOf("MANAGE_WARD_MEETINGS"),"Events"),
        WorkspaceAction("Women's programmes","Manage development programmes and participants","programs/women",setOf("MANAGE_PROGRAMS_WOMEN"),"Programmes"),
        WorkspaceAction("Youth programmes","Manage youth development programmes","programs/youth",setOf("MANAGE_PROGRAMS_YOUTH"),"Programmes"),
        WorkspaceAction("Welfare programmes","Manage support programmes and beneficiaries","programs/welfare",setOf("MANAGE_PROGRAMS_WELFARE"),"Programmes"),
        WorkspaceAction("State finance","Income, expenses and donation transitions","finance",setOf("VIEW_FINANCE"),"Finance"),
        WorkspaceAction("Financial reports","Review authoritative financial records","financial_reports",setOf("VIEW_FINANCE"),"Finance"),
        WorkspaceAction("Audit reports","Draft, submit or view authorized audit reports","audit_reports",setOf("MANAGE_AUDIT_REPORTS","VIEW_AUDIT_REPORTS"),"Finance"),
        WorkspaceAction("Discipline & legal review","Authorized disciplinary and legal workflows","discipline",setOf("VIEW_DISCIPLINE","LEGAL_REVIEW","CREATE_DISCIPLINE"),"Governance"),
        WorkspaceAction("Announcements","Create scoped organizational notices","announcements",setOf("CREATE_ANNOUNCEMENT"),"Governance"),
        WorkspaceAction("Manage public content","Opportunities, initiatives and advocacy","management",setOf("MANAGE_HUBS","MANAGE_PATRONS","MANAGE_FAQ"),"Governance"),
        WorkspaceAction("Partnerships & outreach","Log and follow up organizational engagements","outreach",setOf("MANAGE_OUTREACH"),"Governance"),
    )
    fun forUser(user:UserDto)=if(user.status!="VERIFIED" || user.access.telegram_required&&!user.access.telegram_verified) emptyList() else all.filter { action -> (action.route=="mobilization"&&"mobilization" in user.mobile_tools)||action.capabilities.any { it in user.capabilities } }.distinctBy{it.route}
    fun canNavigate(user:UserDto,route:String):Boolean {
        val public=setOf("home","dashboard","workspace","received_announcements","more","profile","notifications","change_password","news_member","opportunities_member","community","civic","advocacy","leadership","patrons","impact","about","conduct","support","contact","faq","gallery")
        if(route in public||route.startsWith("article/"))return true
        if(user.status!="VERIFIED" || user.access.telegram_required&&!user.access.telegram_verified)return false
        if(route=="mobilization")return "mobilization" in user.mobile_tools
        if(route.startsWith("edit_article/"))return "WRITE_ARTICLES" in user.capabilities
        if(route.startsWith("member_admin/"))return "STAFF_ADMIN" in user.capabilities
        if(route=="attendance")return "RECORD_ATTENDANCE" in user.capabilities
        if(route.startsWith("event/"))return "VIEW_EVENTS" in user.capabilities
        if(route.startsWith("resource/")) {
            val kind=route.removePrefix("resource/")
            val cap=when(kind){"opportunities","community","advocacy"->"MANAGE_HUBS";"patrons"->"MANAGE_PATRONS";"faq"->"MANAGE_FAQ";"donations"->"RECORD_DONATION";"expenses"->"RECORD_EXPENSE";"financial-reports"->"MANAGE_FINANCIAL_REPORTS";"audit-reports"->"MANAGE_AUDIT_REPORTS";"outreach"->"MANAGE_OUTREACH";"events"->"MANAGE_EVENTS";"ward-meetings"->"MANAGE_WARD_MEETINGS";"programs_women"->"MANAGE_PROGRAMS_WOMEN";"programs_youth"->"MANAGE_PROGRAMS_YOUTH";"programs_welfare"->"MANAGE_PROGRAMS_WELFARE";else->null}
            return cap!=null&&cap in user.capabilities
        }
        if(route.startsWith("create_program/"))return all.any { it.route==route.replace("create_program/","programs/")&&it.capabilities.any { c -> c in user.capabilities } }
        return forUser(user).any {it.route==route}
    }
}
