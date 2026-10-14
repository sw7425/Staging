package com.kpn.mobile.core
import com.kpn.mobile.data.UserDto
import org.junit.Test
import org.junit.Assert.*
class MobilizationPolicyTest {
 @Test fun roleNameAloneNeverInventsMobilizationPermission(){val user=UserDto(role="STATE",tier="STATE",role_title="President",status="VERIFIED");assertFalse(WorkspaceActions.canNavigate(user,"mobilization"))}
 @Test fun exactServerToolAuthorizationControlsNativeAction(){val user=UserDto(role="LGA",tier="LGA",status="VERIFIED",mobile_tools=listOf("mobilization"));assertTrue(WorkspaceActions.canNavigate(user,"mobilization"));assertTrue(WorkspaceActions.forUser(user).any{it.route=="mobilization"})}
}
