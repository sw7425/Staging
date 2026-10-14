package com.kpn.mobile.core
import org.junit.Assert.*
import org.junit.Test
class RolePoliciesTest{@Test fun titles(){assertEquals(41,RolePolicies.canonicalTitles.size)};@Test fun general(){assertEquals(Tier.GENERAL,RolePolicies.forTitle(null).tier)}}

class MediaDirectorPolicyTest {
 @Test fun mediaDirectorHasConfirmedFullPolicy() {
  assertEquals(Capability.entries.toSet(),RolePolicies.forTitle("Director of Media & Communications").capabilities)
 }
}
