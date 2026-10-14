package com.kpn.mobile.core
import org.junit.Test
import org.junit.Assert.*
class ReadFreshnessTest {
 private var time=100L
 private val cache=ReadFreshness{time}
 @Test fun firstReadAlwaysFetches(){assertTrue(cache.shouldFetch("news","all",60_000))}
 @Test fun freshRepeatUsesExistingData(){cache.succeeded("news","all");assertFalse(cache.shouldFetch("news","all",60_000))}
 @Test fun changedFilterAlwaysFetches(){cache.succeeded("news","all");assertTrue(cache.shouldFetch("news","education",60_000))}
 @Test fun expiryFetches(){cache.succeeded("news","all");time+=60_000;assertTrue(cache.shouldFetch("news","all",60_000))}
 @Test fun explicitRefreshBypassesFreshness(){cache.succeeded("news","all");assertTrue(cache.shouldFetch("news","all",60_000,true))}
 @Test fun failedRequestIsNotRecordedAsSuccessful(){assertTrue(cache.shouldFetch("home","home",60_000));assertTrue(cache.shouldFetch("home","home",60_000))}
 @Test fun accountBoundaryInvalidatesReadState(){cache.succeeded("dashboard","dashboard");cache.clear();assertTrue(cache.shouldFetch("dashboard","dashboard",30_000))}
 @Test fun homeAndOverviewAlwaysHaveSeparateRootDestinations(){assertTrue("home" in SignedInNavigation.rootRoutes);assertTrue("dashboard" in SignedInNavigation.rootRoutes);assertEquals(5,SignedInNavigation.rootRoutes.distinct().size)}
 @Test fun signedInHomeLinksUseRegisteredNativeRoutes(){assertEquals("news_member",SignedInNavigation.publicRoute(true,"news"));assertEquals("opportunities_member",SignedInNavigation.publicRoute(true,"opportunities"));assertEquals("dashboard",SignedInNavigation.publicRoute(true,"register"));assertEquals("news",SignedInNavigation.publicRoute(false,"news"))}
 @Test fun pendingMembershipKeepsHomeButCannotEnterOperationalWork(){val u=com.kpn.mobile.data.UserDto(status="PENDING",capabilities=listOf("VIEW_FINANCE"));assertTrue(WorkspaceActions.canNavigate(u,"home"));assertTrue(WorkspaceActions.canNavigate(u,"dashboard"));assertFalse(WorkspaceActions.canNavigate(u,"finance"));assertTrue(WorkspaceActions.forUser(u).isEmpty())}
}
