package com.kpn.mobile.ui
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class PublicDirectoryTest {
 @Test fun directoryHasNoDuplicateDestinations(){val routes=publicDirectoryGroups.flatMap{it.entries}.map{it.route};assertEquals(routes.size,routes.toSet().size)}
 @Test fun rootsAndRegistrationAreNotRepeated(){val routes=publicDirectoryGroups.flatMap{it.entries}.map{it.route};assertTrue(routes.none{it in listOf("home","news","opportunities","register","more_public")})}
 @Test fun requiredSecondaryDestinationsRemain(){assertEquals(setOf("leadership","patrons","community","civic","about","impact","gallery","conduct","faq","contact","support","settings"),publicDirectoryGroups.flatMap{it.entries}.map{it.route}.toSet())}
 @Test fun settingsExistsInBothNavigationGraphs(){val s=File("src/main/java/com/kpn/mobile/ui/KpnApp.kt").readText();assertEquals(2,Regex("composable\\(\\s*\"settings\"").findAll(s).count())}
 @Test fun unwantedHeaderLabelsAreRemoved(){assertFalse(File("src/main/java/com/kpn/mobile/ui/KpnComponents.kt").readText().contains("KPN · KEBBI"));assertFalse(File("src/main/java/com/kpn/mobile/ui/PublicScreens.kt").readText().contains("KpnTopBar(\"Explore KPN\")"))}
}
