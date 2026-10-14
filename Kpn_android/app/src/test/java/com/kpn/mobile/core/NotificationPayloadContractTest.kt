package com.kpn.mobile.core
import org.junit.Assert.*
import org.junit.Test
class NotificationPayloadContractTest {
 @Test fun actualBackendRouteKeyOpensNewsStory(){assertEquals("article/story",DeepLinkParser.fromPayload(mapOf("route" to "/newsroom/story/","event" to "INFO","notification_id" to "9")).route)}
 @Test fun actualApprovalPayloadOpensDashboard(){assertEquals("dashboard",DeepLinkParser.fromPayload(mapOf("route" to "/account/dashboard/","event" to "MEMBER_APPROVED","target_type" to "member","target_id" to "42")).route)}
 @Test fun reportTargetWorksWithoutAUrl(){assertEquals("reports",DeepLinkParser.fromPayload(mapOf("target_type" to "report","target_id" to "42")).route)}
 @Test fun externalUrlCannotBypassSafetyWithApprovalEvent(){assertEquals("notifications",DeepLinkParser.fromPayload(mapOf("route" to "https://evil.test/","event" to "MEMBER_APPROVED")).route)}
 @Test fun explicitLinkTakesPrecedenceOverLegacyRoute(){assertEquals("opportunities_member",DeepLinkParser.fromPayload(mapOf("link" to "/opportunities/","route" to "/newsroom/")).route)}
}
