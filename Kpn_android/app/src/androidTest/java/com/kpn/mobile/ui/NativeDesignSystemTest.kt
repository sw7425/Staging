package com.kpn.mobile.ui
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.kpn.mobile.data.Member
import com.kpn.mobile.data.UserDto
import com.kpn.mobile.core.Result
import org.junit.*
import java.io.File
import android.graphics.Bitmap

class NativeDesignSystemTest {
 @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private fun capture(name:String){compose.waitForIdle();val i=InstrumentationRegistry.getInstrumentation();val b=i.uiAutomation.takeScreenshot();val d=File(i.targetContext.getExternalFilesDir(null),"native_qa");d.mkdirs();File(d,"$name.png").outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()}
 @Test fun approvalProgressDisablesBothButtons(){compose.setContent{KpnTheme{MemberDecisionDialog(Member(id=50,first_name="Isolated",last_name="Test",status="PENDING",role_title="Ward Adviser"),"approve",true,null,{error("Duplicate submission")},{error("Dismissed in flight")})}};compose.onNodeWithText("Please wait…").assertIsNotEnabled();compose.onNodeWithText("Cancel").assertIsNotEnabled();compose.onNodeWithText("Submitting decision…").assertIsDisplayed();capture("approval_progress")}
 @Test fun darkLargeTypePreservesStatusAndActionLabels(){compose.setContent{val density=LocalDensity.current;CompositionLocalProvider(LocalDensity provides Density(density.density,1.8f)){KpnTheme(darkTheme=true){Surface{Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){MemberIdentityPanel(UserDto(id=99,first_name="Isolated native",last_name="accessibility fixture",status="PENDING"),"Director of Media & Communications","A long organizational jurisdiction label");Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){StatusBadge("VERIFIED");StatusBadge("PENDING")};InfoBanner("Your request has been recorded.",tone=BannerTone.SUCCESS);ActionTileGrid(listOf(ActionTile("Review applicants in your jurisdiction","members",actionIcon("members")))){} }}}}};compose.onNodeWithText("Director of Media & Communications").assertExists();compose.onAllNodesWithText("PENDING").assertCountEquals(2);capture("dark_large_type");compose.onNodeWithText("Review applicants in your jurisdiction").performScrollTo().assertIsDisplayed();capture("dark_large_action")}
 @Test fun failureDialogKeepsRetryAndServerReasonVisible(){compose.setContent{KpnTheme{MemberDecisionDialog(Member(id=50,first_name="Isolated",last_name="Test",status="PENDING",role_title="Ward Adviser"),"approve",false,Result.Err("outside_jurisdiction","Applicant is outside your authorized jurisdiction.",403),{}, {})}};compose.onNodeWithText("Applicant is outside your authorized jurisdiction.").assertIsDisplayed();compose.onNodeWithText("Retry approval").assertIsEnabled();capture("dialog_failure_light")}
}
