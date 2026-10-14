package com.kpn.mobile.ui

import com.kpn.mobile.core.RolePolicies
import com.kpn.mobile.core.Tier
import org.junit.Assert.*
import org.junit.Test

class MemberHeaderLabelsTest {
    @Test fun allTwentyStateRolesUseExecutiveCaption() {
        val roles = RolePolicies.canonicalTitles.filter { RolePolicies.forTitle(it).tier == Tier.STATE }
        assertEquals(20, roles.size)
        roles.forEach { assertEquals(it, "Executive", memberHeaderTierLabel("STATE")) }
    }
    @Test fun remainingOrganizationalTiersUseTheirOwnCaptions() {
        assertEquals("Zonal", memberHeaderTierLabel("ZONAL"))
        assertEquals("LGA", memberHeaderTierLabel("LGA"))
        assertEquals("Ward", memberHeaderTierLabel("WARD"))
    }
    @Test fun generalAndUnknownTiersDoNotInventExecutiveStatus() {
        assertEquals("KPN membership", memberHeaderTierLabel("GENERAL"))
        assertEquals("KPN membership", memberHeaderTierLabel(""))
        assertEquals("KPN membership", memberHeaderTierLabel("UNKNOWN"))
    }
    @Test fun tierCaptionAcceptsLowercaseWithoutChangingScope() {
        assertEquals("Executive", memberHeaderTierLabel("state"))
        assertEquals("Ward", memberHeaderTierLabel("ward"))
    }
}
