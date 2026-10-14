package com.kpn.mobile.core

import com.kpn.mobile.data.UserDto
import org.junit.Assert.*
import org.junit.Test
class WorkspacePolicyTest {
    @Test fun publicCollectionNotificationLinksRemainNative(){assertEquals("news_member",DeepLinkParser.parse(null,"/newsroom/",null,null).route);assertEquals("opportunities_member",DeepLinkParser.parse(null,"https://www.kpn.com.ng/opportunities/",null,null).route)}
    @Test fun externalCollectionLinksStayRejected(){assertEquals("notifications",DeepLinkParser.parse(null,"https://evil.test/newsroom/",null,null).route)}

    @Test fun everyCanonicalRoleHasAnActualWebsiteTemplate(){assertEquals(41,RoleWorkspaces.roles.size);RoleWorkspaces.roles.values.forEach{assertTrue(it.sourceTemplate.isNotBlank())};assertEquals(RolePolicies.canonicalTitles,RoleWorkspaces.roles.values.map{it.title}.toSet())}
    @Test fun viewEventDoesNotAuthorizeCreatingOne(){val user=UserDto(status="VERIFIED",tier="GENERAL",capabilities=listOf("VIEW_EVENTS"));assertTrue(WorkspaceActions.canNavigate(user,"events"));assertFalse(WorkspaceActions.canNavigate(user,"create_event"));assertFalse(WorkspaceActions.canNavigate(user,"attendance"))}
    @Test fun writingArticleDoesNotAuthorizeReview(){val user=UserDto(status="VERIFIED",tier="WARD",capabilities=listOf("WRITE_ARTICLES"));assertTrue(WorkspaceActions.canNavigate(user,"my_articles"));assertFalse(WorkspaceActions.canNavigate(user,"editorial"))}
    @Test fun financeViewerDoesNotAuthorizeRecordingExpenses(){val user=UserDto(status="VERIFIED",tier="STATE",capabilities=listOf("VIEW_FINANCE"));assertTrue(WorkspaceActions.canNavigate(user,"finance"));assertFalse(WorkspaceActions.canNavigate(user,"resource/expenses"))}
    @Test fun programKindIsPermissionSpecific(){val user=UserDto(status="VERIFIED",tier="LGA",capabilities=listOf("MANAGE_PROGRAMS_WOMEN"));assertTrue(WorkspaceActions.canNavigate(user,"programs/women"));assertFalse(WorkspaceActions.canNavigate(user,"programs/youth"))}
    @Test fun externalNotificationLinksAreRejected(){assertEquals("notifications",DeepLinkParser.parse(null,"https://evil.test/newsroom/story/",null,null).route)}
    @Test fun canonicalKpnArticleLinksAreRecognized(){assertEquals("article/story",DeepLinkParser.parse(null,"https://www.kpn.com.ng/newsroom/story/",null,null).route)}
    @Test fun protocolRelativeExternalNotificationIsRejected(){assertEquals("notifications",DeepLinkParser.parse(null,"//evil.test/newsroom/story/",null,null).route)}
    @Test fun customSchemeRoutesAreAllowlisted(){assertEquals("event/42",DeepLinkParser.parse("kpn://open?route=event%2F42",null,null,null).route);assertEquals("notifications",DeepLinkParser.parse("kpn://open?route=resource%2Fexpenses",null,null,null).route)}
    @Test fun stateScopeDoesNotUseResidentialWard(){val user=UserDto(status="VERIFIED",tier="STATE",zone_name="Residence zone",lga_name="Residence LGA",ward_name="Residence ward");assertEquals("Kebbi State",user.jurisdictionLabel)}
}
