package com.kpn.mobile.ui
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class NavigationPresentationTest {
 @Test fun accountDoesNotRepeatRootShortcuts(){val s=File("src/main/java/com/kpn/mobile/ui/MemberScreens.kt").readText().substringAfter("internal fun MoreScreen");assertFalse(s.contains("ActionTile(\"Home\""));assertFalse(s.contains("ActionTile(\"Overview\""));listOf("profile","notifications","change_password","received_announcements").forEach{assertTrue(s.contains("nav.navigate(\"$it\")"))}}
 @Test fun supplementalNetworkTilesDoNotDuplicateExistingEntrances(){assertEquals(listOf("opportunities_member"),memberUtilityTiles.map{it.route})}
}
