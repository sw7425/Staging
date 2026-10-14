package com.kpn.mobile.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class PublicNavigationLabelTest {
    @Test fun opportunitiesUseTheSamePublicFeatureNameAsTheWebsite() {
        assertEquals("Opportunities", PUBLIC_OPPORTUNITIES_LABEL)
    }
}