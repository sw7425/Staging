package com.kpn.mobile.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.kpn.mobile.data.*
import org.junit.*
import android.graphics.Bitmap
import java.io.File

/** Presentation-only fixtures: official public narrative, explicitly isolated account identity. */
class NativeAccountHomeTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private val home=HomeContent("Kebbi Progressive Youth Network","One Voice, One Change.","Connecting communities, amplifying civic voices, sharing opportunities, and promoting positive development across Kebbi State.",listOf(HomePillar("Media & Information","Verified grassroots reporting, breaking news, and combating misinformation.","news"),HomePillar("Community Development","Interventions, welfare programs, and driving localized positive change.","community")))
 private fun capture(name:String){compose.waitForIdle();val i=InstrumentationRegistry.getInstrumentation();val b=i.uiAutomation.takeScreenshot();val d=File(i.targetContext.getExternalFilesDir(null),"native_qa");d.mkdirs();File(d,"$name.png").outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()}
 @Test fun guestHomeHasWorkingOfficialActions(){var joined=false;var news=false;compose.setContent{KpnTheme{Surface{Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())){HomeIntroduction(home,false,{joined=true},{news=true});HomePillars(home.pillars){}}}}};capture("home_public_refined");compose.onNodeWithText("Join the Network").performClick();compose.onNodeWithText("Explore the Newsroom").performClick();Assert.assertTrue(joined&&news)}
 @Test fun signedInHomeAtLargeDarkTextKeepsNarrativeAndPillars(){compose.setContent{val d=LocalDensity.current;CompositionLocalProvider(LocalDensity provides Density(d.density,1.8f)){KpnTheme(darkTheme=true){Surface{Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())){HomeIntroduction(home,true,{},{});Column(Modifier.padding(20.dp)){HomePillars(home.pillars){}}}}}}};compose.onNodeWithText(home.organization).assertExists();compose.onNodeWithText("Join the Network").assertDoesNotExist();capture("home_dark_large_refined");compose.onNodeWithText("Media & Information").performScrollTo().assertIsDisplayed();capture("home_pillars_dark_large_refined")}
 @Test fun accountIdentityAndSettingsWrapAtLargeText(){var managed=false;compose.setContent{val d=LocalDensity.current;CompositionLocalProvider(LocalDensity provides Density(d.density,1.8f)){KpnTheme(darkTheme=true){Surface{Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(20.dp)){AccountIdentity(UserDto(id=99,username="isolated_accessibility",first_name="Isolated account",last_name="accessibility fixture",role="STATE",tier="STATE",role_title="Director of Media & Communications",status="VERIFIED")){managed=true};AccountControlGroup{AccountControl("Password & security",Icons.Default.Lock){}}}}}}};capture("account_dark_large_refined");compose.onNodeWithText("Manage profile").performScrollTo().performClick();Assert.assertTrue(managed);compose.onNodeWithText("Password & security").performScrollTo().assertIsDisplayed();capture("account_controls_dark_large_refined")}
}
