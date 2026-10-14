package com.kpn.mobile.core
import com.kpn.mobile.data.UserDto
import org.junit.Test
import org.junit.Assert.*
class AccessContextTest {
 private val user=UserDto(id=42,tier="STATE",status="VERIFIED",dashboard="finance_director",capabilities=listOf("VIEW_FINANCE"))
 @Test fun ordinaryProfileTextDoesNotInvalidateAccess(){assertTrue(sameAccessContext(user,user.copy(first_name="Updated")))}
 @Test fun roleDemotionInvalidatesPreviousPrivateData(){assertFalse(sameAccessContext(user,user.copy(tier="WARD",dashboard="ward_community_lead",capabilities=emptyList())))}
 @Test fun jurisdictionTransferInvalidatesPreviousPrivateData(){assertFalse(sameAccessContext(user.copy(tier="LGA",lga=1),user.copy(tier="LGA",lga=2)))}
 @Test fun pendingOrToolRevocationInvalidatesPreviousPrivateData(){assertFalse(sameAccessContext(user,user.copy(status="PENDING")));assertFalse(sameAccessContext(user.copy(mobile_tools=listOf("mobilization")),user))}
}
