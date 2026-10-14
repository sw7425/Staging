package com.kpn.mobile.core
import com.kpn.mobile.data.UserDto

enum class Tier { STATE, ZONAL, LGA, WARD, GENERAL }
enum class Capability { MEMBERS, APPROVE_MEMBERS, REPORTS, REVIEW_REPORTS, EDITORIAL, MEDIA, EVENTS, MINUTES, PROGRAMS, FINANCE, DISCIPLINE, PUBLIC_HUBS, WARD_MEETINGS }
data class RolePolicy(val tier:Tier,val capabilities:Set<Capability>,val telegramRequired:Boolean,val serverCapabilities:Set<String> = emptySet())
object RolePolicies {
 private val stateBase=setOf(Capability.MEMBERS,Capability.REPORTS)
 private val map=mapOf(
 "President" to RolePolicy(Tier.STATE,stateBase+setOf(Capability.APPROVE_MEMBERS,Capability.REVIEW_REPORTS,Capability.DISCIPLINE,Capability.FINANCE),true),
 "Vice President" to RolePolicy(Tier.STATE,stateBase+setOf(Capability.APPROVE_MEMBERS,Capability.REVIEW_REPORTS,Capability.DISCIPLINE),true),
 "General Secretary" to RolePolicy(Tier.STATE,stateBase+setOf(Capability.EVENTS,Capability.MINUTES),true),
 "Assistant General Secretary" to RolePolicy(Tier.STATE,stateBase+Capability.EVENTS,true),
 "Director of Monitoring & Compliance" to RolePolicy(Tier.STATE,stateBase+setOf(Capability.REVIEW_REPORTS,Capability.DISCIPLINE),true),
 "Director of Legal Affairs & Ethics" to RolePolicy(Tier.STATE,stateBase+Capability.DISCIPLINE,true),
 "Director of Finance" to RolePolicy(Tier.STATE,stateBase+Capability.FINANCE,true),
 "Finance Operations Officer" to RolePolicy(Tier.STATE,stateBase+Capability.FINANCE,true),
 "Director of Audit & Accountability" to RolePolicy(Tier.STATE,stateBase+Capability.FINANCE,true),
 "Director of Community Engagement" to RolePolicy(Tier.STATE,stateBase+setOf(Capability.PROGRAMS,Capability.PUBLIC_HUBS),true),
 "Assistant Director of Community Engagement" to RolePolicy(Tier.STATE,stateBase+Capability.PROGRAMS,true),
 "Director of Programmes & Events" to RolePolicy(Tier.STATE,stateBase+setOf(Capability.PROGRAMS,Capability.EVENTS),true),
 "Assistant Director of Programmes & Events" to RolePolicy(Tier.STATE,stateBase+setOf(Capability.PROGRAMS,Capability.EVENTS),true),
 "Director of Member Support & Welfare" to RolePolicy(Tier.STATE,stateBase+Capability.PROGRAMS,true),
 "Director of Youth Development" to RolePolicy(Tier.STATE,stateBase+Capability.PROGRAMS,true),
 "Director of Women's Development" to RolePolicy(Tier.STATE,stateBase+Capability.PROGRAMS,true),
 "Assistant Director of Women's Development" to RolePolicy(Tier.STATE,stateBase+Capability.PROGRAMS,true),
 "Director of Media & Communications" to RolePolicy(Tier.STATE,Capability.entries.toSet(),true),
 "Assistant Director of Media & Communications" to RolePolicy(Tier.STATE,stateBase+setOf(Capability.EDITORIAL,Capability.MEDIA),true),
 "Director of Public Relations & Partnerships" to RolePolicy(Tier.STATE,stateBase+setOf(Capability.MEDIA,Capability.PUBLIC_HUBS),true),
 "Senatorial Director" to RolePolicy(Tier.ZONAL,setOf(Capability.MEMBERS,Capability.APPROVE_MEMBERS,Capability.REPORTS,Capability.REVIEW_REPORTS),true),
 "Senatorial Administrative Officer" to RolePolicy(Tier.ZONAL,setOf(Capability.MEMBERS,Capability.REPORTS),true),
 "Senatorial Communications Officer" to RolePolicy(Tier.ZONAL,setOf(Capability.REPORTS,Capability.MEDIA),true),
 "LGA Network Lead" to RolePolicy(Tier.LGA,setOf(Capability.MEMBERS,Capability.APPROVE_MEMBERS,Capability.REPORTS,Capability.REVIEW_REPORTS),true),
 "Ward Community Lead" to RolePolicy(Tier.WARD,setOf(Capability.MEMBERS,Capability.REPORTS,Capability.WARD_MEETINGS),true),
 "Ward Administrative Officer" to RolePolicy(Tier.WARD,setOf(Capability.MEMBERS,Capability.REPORTS,Capability.WARD_MEETINGS),false)
 )
 private val lgaTitles=setOf("LGA Administrative Officer","LGA Programmes Officer","LGA Finance Officer","LGA Communications Officer","LGA Monitoring Officer","LGA Women's Development Officer","LGA Member Support Officer","LGA Community Engagement Officer","LGA Adviser")
 private val wardTitles=setOf("Ward Programmes Officer","Ward Finance Officer","Ward Communications Officer","Ward Monitoring Officer","Ward Community Support Officer","Ward Adviser")
 fun forTitle(title:String?):RolePolicy = map[title] ?: when(title){in lgaTitles->RolePolicy(Tier.LGA,setOf(Capability.MEMBERS,Capability.REPORTS),false); in wardTitles->RolePolicy(Tier.WARD,setOf(Capability.MEMBERS,Capability.REPORTS),false); else->RolePolicy(Tier.GENERAL,emptySet(),false)}
 fun fromUser(user:UserDto):RolePolicy{val tier=runCatching{Tier.valueOf(user.tier)}.getOrDefault(Tier.GENERAL);val source=user.capabilities.toSet();val caps=buildSet{if("VIEW_MEMBERS" in source)add(Capability.MEMBERS);if("REVIEW_APPLICANTS" in source)add(Capability.APPROVE_MEMBERS);if("SUBMIT_REPORTS" in source)add(Capability.REPORTS);if("REVIEW_REPORTS" in source||"OVERSIGHT_REPORTS" in source)add(Capability.REVIEW_REPORTS);if("WRITE_ARTICLES" in source||"REVIEW_ARTICLES" in source)add(Capability.EDITORIAL);if("UPLOAD_MEDIA" in source||"REVIEW_MEDIA" in source)add(Capability.MEDIA);if("VIEW_EVENTS" in source||"MANAGE_EVENTS" in source)add(Capability.EVENTS);if("EDIT_MINUTES" in source)add(Capability.MINUTES);if(source.any{it.startsWith("MANAGE_PROGRAM")})add(Capability.PROGRAMS);if("VIEW_FINANCE" in source)add(Capability.FINANCE);if(source.any{it.contains("DISCIPLINE")||it=="LEGAL_REVIEW"})add(Capability.DISCIPLINE);if("MANAGE_HUBS" in source||"MANAGE_PATRONS" in source)add(Capability.PUBLIC_HUBS);if("VIEW_WARD_MEETINGS" in source)add(Capability.WARD_MEETINGS)};return RolePolicy(tier,caps,user.access.telegram_required,source)}
 val canonicalTitles:Set<String> get()=map.keys+lgaTitles+wardTitles
}
