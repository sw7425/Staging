package com.kpn.mobile.core

/** Exact category values extracted from the supplied Django models. */
object ContentChoices {
 fun label(choices:List<Pair<String,String>>,value:String)=choices.firstOrNull{it.first==value}?.second ?: value.replace('_',' ')

 val news=listOf("COMMUNITY" to "Community", "EDUCATION" to "Education", "AGRICULTURE" to "Agriculture", "HEALTH" to "Health", "SECURITY" to "Security", "INFRASTRUCTURE" to "Infrastructure", "MARKETS" to "Markets & Business", "YOUTH" to "Youth", "WOMEN" to "Women", "ENVIRONMENT" to "Environment", "HUMAN_INTEREST" to "Human Interest", "CULTURE" to "Culture", "TRADITIONAL" to "Traditional Institutions", "GOVERNANCE" to "Government & Governance", "EMERGENCY" to "Emergency", "CIVIC" to "Civic", "GENERAL" to "General")
 val communityReports=listOf("COMMUNITY" to "Community", "EDUCATION" to "Education", "AGRICULTURE" to "Agriculture", "HEALTH" to "Health", "SECURITY" to "Security", "INFRASTRUCTURE" to "Infrastructure", "MARKETS" to "Markets & Business", "YOUTH" to "Youth", "WOMEN" to "Women", "ENVIRONMENT" to "Environment", "HUMAN_INTEREST" to "Human Interest", "CULTURE" to "Culture", "TRADITIONAL" to "Traditional Institutions", "GOVERNMENT" to "Government & Governance", "EMERGENCY" to "Emergency", "OPPORTUNITY" to "Opportunity")
 val opportunities=listOf("SCHOLARSHIP" to "Scholarships & Education", "GRANT" to "Grants & Funding", "JOB" to "Jobs & Employment", "INTERNSHIP" to "Internships", "VOLUNTEER" to "Volunteer Opportunities", "ENTREPRENEURSHIP" to "Entrepreneurship", "TRAINING" to "Skills & Training", "EMPOWERMENT" to "Empowerment Programs", "FELLOWSHIP" to "Fellowships & Leadership")
 val verification=listOf("DEVELOPING" to "KPN Developing", "CONFIRMED" to "KPN Confirmed", "VERIFIED" to "KPN Verified", "COMMUNITY_ALERT" to "KPN Community Alert")
 val reporterCredit=listOf("KPN_REPORTER" to "KPN Community Reporter", "KPN_CORRESPONDENT" to "KPN Ward Correspondent", "KPN_LGA" to "KPN LGA Network Team", "KPN_STATE" to "KPN State Team", "KPN_EDITORIAL" to "KPN Editorial Team")
}
