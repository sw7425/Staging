package com.kpn.mobile.core
import org.junit.Test
import org.junit.Assert.*
import com.kpn.mobile.data.UserDto
class SessionBoundaryTest {
 private val cached=AppState(booting=false,user=UserDto(id=42,status="VERIFIED"),resourceRows=listOf(mapOf("private" to "old account")),participantIds=listOf(42),unreadCount=7)
 @Test fun expiredSessionRemovesAllPrivateState(){val s=stateAfterFailure(cached,"boot",Result.Err("auth","Expired",401),null);assertNull(s.user);assertTrue(s.resourceRows.isEmpty());assertTrue(s.participantIds.isEmpty());assertNull(s.unreadCount);assertEquals(setOf("boot"),s.errors.keys)}
 @Test fun networkErrorPreservesCurrentAccount(){val s=stateAfterFailure(cached,"profile",Result.Err("network","Offline"),"valid");assertEquals(cached.user,s.user);assertEquals(cached.resourceRows,s.resourceRows)}
 @Test fun oldRequestCannotClearNewTokenState(){val s=stateAfterFailure(cached,"old",Result.Err("auth","Old request",401),"new-account-token");assertEquals(cached.user,s.user)}
 @Test fun retirementCredentialsDoNotModifyAnotherStore(){val current=RetirementTokens("new","new-refresh");val old=RetirementTokens("old","old-refresh");old.save("retiring","retiring-refresh");old.clearTokens();assertEquals("new",current.access());assertEquals("new-refresh",current.refresh());assertNull(old.access())}
}
