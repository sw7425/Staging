package com.kpn.mobile.ui
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelStore
import androidx.test.platform.app.InstrumentationRegistry
import com.kpn.mobile.KpnApplication
import com.kpn.mobile.core.*
import org.junit.*
import android.graphics.Bitmap
import java.io.File

/** Representative full-shell checks against the isolated real backend, not production users. */
class NativeShellTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private lateinit var app:KpnApplication;private lateinit var vm:AppViewModel;private lateinit var previous:ApiClient
 private val store=ViewModelStore()
 @After fun cleanup(){compose.runOnIdle{store.clear();app.session.clear();app.api=previous}}
 private fun capture(name:String){compose.waitForIdle();val i=InstrumentationRegistry.getInstrumentation();val b=i.uiAutomation.takeScreenshot();val d=File(i.targetContext.getExternalFilesDir(null),"native_qa");d.mkdirs();File(d,"$name.png").outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()}
 private fun root(label:String){compose.onNode(hasText(label) and hasClickAction()).performClick()}
 @Test fun homeOverviewWorkAndAccountRemainDistinctAndAccessible(){
  compose.runOnIdle{app=compose.activity.application as KpnApplication;previous=app.api;app.session.clear();app.api=ApiClient(app.session,"http://10.0.2.2:8000/api/v1/");vm=AppViewModel(app);store.put("shell-qa",vm);vm.login("isolated_native_president","Local-native-QA-only-942!")}
  compose.waitUntil(60_000){vm.state.value.user!=null}
  compose.setContent{KpnTheme{KpnApp(vm)}}
  compose.waitUntil(60_000){vm.state.value.dashboard!=null}
  compose.onNodeWithText("Quick actions").assertExists();capture("member_overview")
  root("Home");compose.waitUntil(60_000){vm.state.value.homeContent!=null};compose.onNodeWithText("Kebbi Progressive Youth Network").assertExists();capture("member_home")
  root("My work");compose.onNodeWithText("Review applicants").assertExists();capture("member_work")
  root("Account");compose.onNodeWithText("My account").assertExists();compose.onAllNodesWithText("Overview").assertCountEquals(1);capture("member_account")
  compose.onNode(hasScrollAction()).performScrollToNode(hasText("Network links"));compose.onNode(hasText("Network links") and hasClickAction()).performClick();capture("member_account_network");compose.onNode(hasScrollAction()).performScrollToNode(hasText("About KPN"));compose.onNode(hasText("About KPN") and hasClickAction()).performClick()
  compose.waitUntil(30_000){compose.onAllNodesWithText("About Kebbi Progressive Youth Network").fetchSemanticsNodes().isNotEmpty()};capture("canonical_about")
 }
}
