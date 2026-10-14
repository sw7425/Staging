package com.kpn.mobile.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.kpn.mobile.KpnApplication
import com.kpn.mobile.core.*
import org.junit.*
import org.junit.Assert.*
import java.io.File
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/** Runs ONLY against the separately prepared isolated Django fixture database. */
class NativeApprovalTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private lateinit var app:KpnApplication
 private lateinit var vm:AppViewModel
 private lateinit var previous:ApiClient
 private val store=ViewModelStore()
 @Before fun prepare(){compose.runOnIdle{app=compose.activity.application as KpnApplication;previous=app.api;app.session.clear();app.api=ApiClient(app.session,"http://10.0.2.2:8000/api/v1/");vm=AppViewModel(app);store.put("native-qa",vm)}}
 @After fun cleanup(){compose.runOnIdle{store.clear();app.session.clear();app.api=previous}}
 private fun login(username:String){compose.runOnIdle{vm.login(username,"Local-native-QA-only-942!")};compose.waitUntil(60_000){vm.state.value.user!=null};compose.setContent{val s by vm.state.collectAsStateWithLifecycle();KpnTheme{MembersScreen(vm,s,rememberNavController(),RolePolicies.fromUser(s.user!!))}};compose.waitUntil(60_000){vm.state.value.members.isNotEmpty()&&!vm.state.value.busy}}
 private fun begin(name:String){compose.onNodeWithText(name).performClick();compose.waitUntil(30_000){vm.state.value.selectedMember!=null};compose.onNodeWithText("Approve",useUnmergedTree=true).performClick();compose.onNodeWithText("Approve membership?").assertIsDisplayed()}
 private fun screenshot(name:String){compose.waitForIdle();val i=InstrumentationRegistry.getInstrumentation();val bitmap=i.uiAutomation.takeScreenshot();val dir=File(i.targetContext.getExternalFilesDir(null),"native_qa");dir.mkdirs();File(dir,"$name.png").outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}
 @Test fun presidentApprovalConfirmsVerifiedAndRefreshesDirectoryAndDashboard(){login("isolated_native_president");begin("Isolated Success");compose.onNodeWithTag("member-decision-confirm").performClick();compose.waitUntil(60_000){vm.state.value.selectedMember?.id==3&&vm.state.value.selectedMember?.status=="VERIFIED"&&"member_decision" !in vm.state.value.loading};compose.onNodeWithText("Membership approved. Member is verified.").assertIsDisplayed();assertFalse(vm.state.value.members.any{it.id==3});compose.onNodeWithText("Directory").performClick();compose.waitUntil(60_000){vm.state.value.members.any{it.id==3&&it.status=="VERIFIED"}&&vm.state.value.dashboard!=null};screenshot("approval_success_directory")}
 @Test fun mediaDirectorCanApproveStateApplicantWithConfirmedFullAccess(){login("isolated_native_media");begin("Isolated Forbidden");compose.onNodeWithTag("member-decision-confirm").performClick();compose.waitUntil(60_000){vm.state.value.selectedMember?.id==5&&vm.state.value.selectedMember?.status=="VERIFIED"&&"member_decision" !in vm.state.value.loading};compose.onNodeWithText("Membership approved. Member is verified.").assertIsDisplayed();assertFalse(vm.state.value.members.any{it.id==5});screenshot("media_director_approval_success")}

 @Test fun serverFailureAllowsRetryWithoutPrematureSuccess(){login("isolated_native_president");begin("Isolated Retry");runBlocking(Dispatchers.IO){java.net.URL("http://10.0.2.2:8000/__qa__/arm503").readText()};compose.onNodeWithTag("member-decision-confirm").performClick();compose.waitUntil(60_000){vm.state.value.errors["member_decision"]?.status==503};assertEquals("PENDING",vm.state.value.selectedMember!!.status);compose.onNodeWithText("KPN is temporarily unavailable. Please retry.").assertIsDisplayed();compose.onNodeWithText("Retry approval").assertIsEnabled();screenshot("approval_retry_visible");compose.onNodeWithText("Retry approval").performClick();compose.waitUntil(60_000){vm.state.value.selectedMember?.id==4&&vm.state.value.selectedMember?.status=="VERIFIED"&&"member_decision" !in vm.state.value.loading};compose.onNodeWithText("Membership approved. Member is verified.").assertIsDisplayed();screenshot("approval_retry_success")}
}
