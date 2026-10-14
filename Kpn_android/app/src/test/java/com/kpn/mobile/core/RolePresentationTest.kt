package com.kpn.mobile.core
import org.junit.Test
import org.junit.Assert.*
class RolePresentationTest {
 @Test fun unsetScopeDoesNotRenderTechnicalNA(){assertEquals("KPN membership",dashboardJurisdiction("N/A","KPN membership"));assertEquals("Actual server jurisdiction",dashboardJurisdiction("Actual server jurisdiction","Residence"))}

 @Test fun categoriesUseActualWebsiteLabels(){assertEquals("General",ContentChoices.label(ContentChoices.news,"GENERAL"));assertEquals("Markets & Business",ContentChoices.label(ContentChoices.news,"MARKETS"));assertEquals("Government & Governance",ContentChoices.label(ContentChoices.communityReports,"GOVERNMENT"));assertEquals("Jobs & Employment",ContentChoices.label(ContentChoices.opportunities,"JOB"))}

 @Test fun contextUsesAuthoritativeLocation(){assertEquals("Supervise Aliero LGA",roleResponsibility("1 Supervise {{ user.lga.name }} LGA","Aliero","QA Ward"));assertEquals("Support QA Ward Ward",roleResponsibility("2 Support {{ user.ward.name }} Ward",null,"QA Ward"))}
 @Test fun noTemplateTokensLeakForAnyCanonicalRole(){RoleWorkspaces.roles.values.flatMap{it.responsibilities}.forEach{assertFalse(roleResponsibility(it,null,null).contains("{{"))}}
 @Test fun generalMemberUsesOrganizationalLabel(){assertEquals("General Member",membershipTierLabel("GENERAL"));assertEquals("Senatorial",membershipTierLabel("ZONAL"))}
}
