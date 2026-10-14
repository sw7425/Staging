package com.kpn.mobile.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import androidx.test.platform.app.InstrumentationRegistry
import com.kpn.mobile.KpnApplication
import com.kpn.mobile.core.*
import org.junit.*
import org.junit.Assert.*
import java.io.File
import android.graphics.Bitmap

/** Runs on the native UI. No production account/login or backend mutation is required. */
class NativeMoreSettingsTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private val viewModels=ViewModelStore()
 private lateinit var app:KpnApplication
 @Before fun setup(){app=compose.activity.application as KpnApplication;app.appearance.setChoice(AppearanceChoice.SYSTEM)}
 @After fun cleanup(){compose.runOnIdle{viewModels.clear();app.appearance.setChoice(AppearanceChoice.SYSTEM)}}
 private fun capture(name:String){compose.waitForIdle();Thread.sleep(1800);val i=InstrumentationRegistry.getInstrumentation();val b=i.uiAutomation.takeScreenshot();val d=File(i.targetContext.getExternalFilesDir(null),"more_settings_qa");d.mkdirs();File(d,"$name.png").outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()}
 private fun content(large:Boolean=false) {
  compose.setContent {
   val density=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(density.density,if(large)1.8f else 1f)) {
    val choice by app.appearance.choice.collectAsStateWithLifecycle()
    KpnTheme(darkTheme=choice.isDark(isSystemInDarkTheme())) {
     val nav=rememberNavController()
     val vm=remember{AppViewModel(app).also{viewModels.put("more-ui",it)}}
     Surface(color=MaterialTheme.colorScheme.background) {
     NavHost(nav,"more_public") {
      composable("more_public") { PublicMoreScreen(vm,AppState(),nav) }
      composable("settings") { SettingsScreen(nav) }
      composable("faq") {
       Column {
        Text("FAQ destination")
        TextButton(onClick={nav.popBackStack()}) { Text("Back to More") }
       }
      }
      composable("login") { Text("Sign-in destination") }
     }
     }
    }
   }
  }
 }
 @Test fun directoryTilesAndSettingsRouteWorkWithoutDuplicateRoots(){content();compose.onNodeWithText("People & community").assertIsDisplayed();compose.onAllNodesWithText("Leadership").assertCountEquals(1);compose.onNodeWithText("Explore KPN").assertDoesNotExist();compose.onNodeWithText("KPN · KEBBI").assertDoesNotExist();capture("more_light_top");compose.onNode(hasScrollAction()).performScrollToNode(hasText("FAQ"));capture("more_light_help");compose.onNodeWithText("FAQ").performClick();compose.onNodeWithText("FAQ destination").assertIsDisplayed();compose.onNodeWithText("Back to More").performClick();compose.onNode(hasScrollAction()).performScrollToNode(hasText("Settings"));compose.onNodeWithText("Settings").performClick();compose.onNodeWithText("Appearance").assertIsDisplayed();capture("settings_light")}
 @Test fun appearanceChangesImmediatelyAndPersistsInSeparateStore(){content();compose.onNode(hasScrollAction()).performScrollToNode(hasText("Settings"));compose.onNodeWithText("Settings").performClick();compose.onNodeWithText("Dark").performClick();assertEquals(AppearanceChoice.DARK,app.appearance.choice.value);assertEquals(AppearanceChoice.DARK,AppearanceStore(app).choice.value);compose.onNodeWithText("Dark").assertIsSelected();capture("settings_dark");compose.onNodeWithText("Light").performClick();compose.onNodeWithText("Light").assertIsSelected();assertEquals(AppearanceChoice.LIGHT,AppearanceStore(app).choice.value);compose.onNodeWithText("System default").performClick();compose.onNodeWithText("System default").assertIsSelected();assertEquals(AppearanceChoice.SYSTEM,AppearanceStore(app).choice.value)}
 @Test fun darkLargeTextKeepsDirectoryAndAppearanceControlsReadable(){app.appearance.setChoice(AppearanceChoice.DARK);content(true);compose.onNodeWithText("Leadership").performScrollTo().assertIsDisplayed();capture("more_dark_large");compose.onNode(hasScrollAction()).performScrollToNode(hasText("Settings"));compose.onNodeWithText("Settings").performClick();compose.onNodeWithText("System default").assertExists();compose.onNodeWithText("Dark").assertIsSelected();capture("settings_dark_large");compose.onNode(hasScrollAction()).performScrollToNode(hasText("Device notification settings"));compose.onNodeWithText("Device notification settings").assertIsDisplayed();capture("settings_device_controls_large")}
}
