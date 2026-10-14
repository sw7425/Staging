package com.kpn.mobile.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.kpn.mobile.data.HomePillar
import org.junit.*
import org.junit.Assert.*
import android.graphics.Bitmap
import java.io.File

/** Isolated presentation tests. No production credentials or server mutations. */
class NativeHeaderPillarsTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val pillars = listOf(
        HomePillar("Media & Information", "Body text must not appear 1", "news"),
        HomePillar("Community Development", "Body text must not appear 2", "community"),
        HomePillar("Youth Empowerment", "Body text must not appear 3", "opportunities"),
        HomePillar("Opportunities Hub", "Body text must not appear 4", "opportunities"),
        HomePillar("Civic Education & Advocacy", "Body text must not appear 5", "civic")
    )
    private fun capture(name: String) {
        compose.waitForIdle(); Thread.sleep(1800)
        val i = InstrumentationRegistry.getInstrumentation()
        val b = i.uiAutomation.takeScreenshot()
        val d = File(i.targetContext.getExternalFilesDir(null), "header_pillars_qa"); d.mkdirs()
        File(d, "$name.png").outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }; b.recycle()
    }
    @Test fun compactPillarsHideOnlyDescriptionsAndKeepEveryAction() {
        var opened = ""
        compose.setContent {
            KpnTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
                        KpnTopBar("KPN network")
                        Text("Our 5 Core Pillars", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(16.dp))
                        HomePillars(pillars) { opened = it }
                    }
                }
            }
        }
        compose.onNodeWithText("KPN network").assertIsDisplayed()
        compose.onNodeWithText("The KPN network").assertDoesNotExist()
        pillars.forEach { p ->
            compose.onNodeWithText(p.body).assertDoesNotExist()
            compose.onNodeWithText(p.title).performScrollTo().performClick()
            assertEquals(p.route, opened)
        }
        capture("pillars_light_compact")
    }
    @Test fun largeDarkTextRetainsTitlesWithoutDescriptions() {
        compose.setContent {
            val d = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(d.density, 1.8f)) {
                KpnTheme(darkTheme = true) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
                            HomePillars(pillars) {}
                        }
                    }
                }
            }
        }
        compose.onNodeWithText(pillars.first().title).assertIsDisplayed()
        capture("pillars_dark_large_top")
        pillars.forEach { p ->
            compose.onNodeWithText(p.title).performScrollTo().assertIsDisplayed()
            compose.onNodeWithText(p.body).assertDoesNotExist()
        }
        capture("pillars_dark_large_bottom")
    }
    @Test fun allTierCaptionsRenderWithoutGeographicalHeaderTags() {
        compose.setContent {
            KpnTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        listOf("STATE", "ZONAL", "LGA", "WARD").forEach { tier ->
                            Text("KPN", style = MaterialTheme.typography.titleMedium)
                            Text(memberHeaderTierLabel(tier), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        listOf("Executive", "Zonal", "LGA", "Ward").forEach { compose.onNodeWithText(it).assertIsDisplayed() }
        compose.onNodeWithText("Kebbi State").assertDoesNotExist()
        capture("tier_header_captions")
    }
}
