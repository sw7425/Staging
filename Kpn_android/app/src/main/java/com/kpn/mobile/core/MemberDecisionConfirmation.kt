package com.kpn.mobile.core
import com.kpn.mobile.data.Member

/** HTTP completion alone is not proof that the requested membership transition occurred. */
internal fun confirmedMemberDecision(id: Int, decision: String, member: Member): Result<Member> {
    val expected = when(decision) { "approve" -> "VERIFIED"; "reject" -> "REJECTED"; else -> null }
    return if(expected!=null && member.id==id && member.status==expected) Result.Ok(member)
    else Result.Err("unconfirmed_decision", "KPN did not confirm the requested membership change. Refresh the member record before trying again.")
}
internal fun stateAfterConfirmedMemberDecision(state: AppState, member: Member): AppState = state.copy(
    busy=false, members=state.members.filter { it.id!=member.id },
    selectedMember=member, errors=state.errors-"member_decision",
)
