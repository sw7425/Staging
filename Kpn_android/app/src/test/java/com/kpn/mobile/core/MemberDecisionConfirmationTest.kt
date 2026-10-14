package com.kpn.mobile.core
import com.kpn.mobile.data.Member
import org.junit.Assert.*
import org.junit.Test
class MemberDecisionConfirmationTest {
 private val pending=Member(id=3,username="local-only",status="PENDING")
 @Test fun approvalRequiresVerifiedReturnedRecord(){assertTrue(confirmedMemberDecision(3,"approve",pending.copy(status="VERIFIED")) is Result.Ok);assertTrue(confirmedMemberDecision(3,"approve",pending) is Result.Err)}
 @Test fun wrongIdentityOrTransitionIsNotSuccess(){assertTrue(confirmedMemberDecision(4,"approve",pending.copy(status="VERIFIED")) is Result.Err);assertTrue(confirmedMemberDecision(3,"reject",pending.copy(status="VERIFIED")) is Result.Err)}
 @Test fun failurePreservesPendingAndSelectedMember(){val state=AppState(booting=false,busy=true,members=listOf(pending),selectedMember=pending);val failed=stateAfterFailure(state,"member_decision",Result.Err("tier_ceiling","Not permitted",403),"token");assertEquals(listOf(pending),failed.members);assertEquals("PENDING",failed.selectedMember!!.status);assertFalse(failed.busy)}
 @Test fun confirmedApprovalUpdatesDetailAndRemovesPendingRow(){val state=AppState(members=listOf(pending),selectedMember=pending);val next=stateAfterConfirmedMemberDecision(state,pending.copy(status="VERIFIED"));assertTrue(next.members.isEmpty());assertEquals("VERIFIED",next.selectedMember!!.status)}
 @Test fun rejectionRequiresRejectedSnapshot(){assertTrue(confirmedMemberDecision(3,"reject",pending.copy(status="REJECTED")) is Result.Ok)}
}
