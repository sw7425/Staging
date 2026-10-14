package com.kpn.mobile.core
import org.junit.Assert.*
import org.junit.Test
class AppearanceChoiceTest {
 @Test fun systemFollowsDevice(){assertFalse(AppearanceChoice.SYSTEM.isDark(false));assertTrue(AppearanceChoice.SYSTEM.isDark(true))}
 @Test fun lightOverridesDarkDevice(){assertFalse(AppearanceChoice.LIGHT.isDark(true));assertFalse(AppearanceChoice.LIGHT.isDark(false))}
 @Test fun darkOverridesLightDevice(){assertTrue(AppearanceChoice.DARK.isDark(false));assertTrue(AppearanceChoice.DARK.isDark(true))}
 @Test fun storedValuesRoundTrip(){AppearanceChoice.entries.forEach{assertEquals(it,AppearanceChoice.fromStored(it.name))}}
 @Test fun absentOrUnrecognizedValueIsSystem(){assertEquals(AppearanceChoice.SYSTEM,AppearanceChoice.fromStored(null));assertEquals(AppearanceChoice.SYSTEM,AppearanceChoice.fromStored("unknown"))}
}
