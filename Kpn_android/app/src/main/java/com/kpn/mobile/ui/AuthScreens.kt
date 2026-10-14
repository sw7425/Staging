package com.kpn.mobile.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kpn.mobile.R
import com.kpn.mobile.core.*
import com.kpn.mobile.data.*

@Composable internal fun LoginScreen(vm:AppViewModel,nav:NavHostController) {
    val s by vm.state.collectAsStateWithLifecycle()
    var username by rememberSaveable { mutableStateOf("") }; var password by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Member sign in",back={nav.popBackStack()})
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().padding(24.dp)) {
            Image(painterResource(R.drawable.kpn_logo),"KPN emblem",Modifier.size(64.dp))
            Spacer(Modifier.height(20.dp));Text("Welcome back",style=MaterialTheme.typography.headlineMedium)
            Text("Use the same username and password as your KPN website account.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(top=8.dp,bottom=24.dp))
            KpnField("Username",username,{username=it},error=s.errors["login"]?.fields?.get("username"))
            KpnField("Password",password,{password=it},password=true,error=s.errors["login"]?.fields?.get("password"))
            ErrorState(s.errors["login"] ?: s.errors["boot"]) { vm.clearError("login");vm.clearError("boot") }
            KpnPrimaryButton("Sign in",s.busy,username.isNotBlank()&&password.isNotBlank()) { vm.login(username,password) }
            TextButton(onClick={nav.navigate("forgot_password")},modifier=Modifier.align(Alignment.End)) { Text("Forgot password?") }
            Spacer(Modifier.height(24.dp));KpnDivider();Text("New to KPN?",style=MaterialTheme.typography.titleSmall,modifier=Modifier.padding(top=20.dp))
            Text("Apply for an available leadership position using the official registration process.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick={nav.navigate("register")},shape=MaterialTheme.shapes.small,modifier=(Modifier.fillMaxWidth().padding(top=12.dp)).heightIn(min=48.dp)) { Text("Join the network") }
        }
    }
}

@Composable internal fun ForgotPasswordScreen(vm:AppViewModel,nav:NavHostController) {
    val s by vm.state.collectAsStateWithLifecycle();var email by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Account recovery",back={nav.popBackStack()})
        Column(Modifier.verticalScroll(rememberScrollState()).imePadding().padding(20.dp)) {
            Text("Reset your password",style=MaterialTheme.typography.headlineSmall)
            Text("Enter the email address associated with your KPN account.",style=MaterialTheme.typography.bodyMedium,modifier=Modifier.padding(vertical=12.dp))
            KpnField("Email address",email,{email=it},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email))
            ErrorState(s.errors["password_reset"]) {vm.clearError("password_reset")}
            if(s.passwordResetSent) InfoBanner("If an account exists, instructions have been sent. Check your inbox and spam folder.")
            Spacer(Modifier.height(16.dp));KpnPrimaryButton("Send reset instructions",s.busy,email.contains("@")) {vm.requestPasswordReset(email)}
        }
    }
}

@Composable internal fun RegisterScreen(vm:AppViewModel,s:AppState,nav:NavHostController) {
    LaunchedEffect(Unit){vm.loadRegistrationData()}
    var step by rememberSaveable { mutableIntStateOf(1) }
    var username by rememberSaveable {mutableStateOf("")};var email by rememberSaveable {mutableStateOf("")}
    var first by rememberSaveable {mutableStateOf("")};var last by rememberSaveable {mutableStateOf("")};var phone by rememberSaveable {mutableStateOf("")}
    var password by remember {mutableStateOf("")};var confirm by remember {mutableStateOf("")};var gender by rememberSaveable {mutableStateOf("")}
    var photo by rememberSaveable {mutableStateOf<String?>(null)};var bio by rememberSaveable {mutableStateOf("")}
    var zone by remember {mutableStateOf<Zone?>(null)};var lga by remember {mutableStateOf<Lga?>(null)};var ward by remember {mutableStateOf<Ward?>(null)};var role by remember {mutableStateOf<RoleDefinition?>(null)}
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->photo=uri?.toString() ?: photo}
    LaunchedEffect(zone?.id,lga?.id,ward?.id){role=null;vm.loadVacancies(zone?.id,lga?.id,ward?.id)}
    val errors=s.errors["register"]?.fields.orEmpty()
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Join the network",back={if(step==1)nav.popBackStack() else step--})
        Row(Modifier.padding(horizontal=20.dp,vertical=12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            repeat(2){i->LinearProgressIndicator(progress={if(i<step)1f else 0f},modifier=Modifier.weight(1f),color=MaterialTheme.colorScheme.primary)}
        }
        Surface(color=MaterialTheme.colorScheme.primaryContainer,shape=MaterialTheme.shapes.small,modifier=Modifier.fillMaxWidth().padding(horizontal=20.dp)) { Text("Step $step of 2 · ${if(step==1) "Personal details" else "Location & position"}",Modifier.padding(12.dp),style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onPrimaryContainer) }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().padding(20.dp)) {
            if(step==1) {
                FormSection("01","Your identity","Your application is reviewed through KPN’s official approval process.")
                Row(verticalAlignment=Alignment.CenterVertically) {
                    KpnImage(photo,"Selected profile portrait",Modifier.size(80.dp),portrait=true)
                    Spacer(Modifier.width(16.dp));Column {Text("Profile photo · required",style=MaterialTheme.typography.titleSmall);Text("Maximum 400 KB after processing",style=MaterialTheme.typography.labelSmall);OutlinedButton(modifier=Modifier.heightIn(min=48.dp), onClick={picker.launch("image/*")},shape=MaterialTheme.shapes.small){Text(if(photo==null) "Choose photo" else "Change photo")}}
                }
                errors["photo"]?.let{Text(it,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)}
                Spacer(Modifier.height(20.dp));KpnField("First name",first,{first=it},error=errors["first_name"]);KpnField("Last name",last,{last=it},error=errors["last_name"])
                KpnField("Username",username,{username=it},error=errors["username"])
                KpnField("Email address",email,{email=it},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email),error=errors["email"])
                KpnField("Phone number",phone,{phone=it},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Phone),error=errors["phone"])
                KpnDropdown("Gender",listOf("M" to "Male","F" to "Female").firstOrNull{it.first==gender},listOf("M" to "Male","F" to "Female"),{it.second},{gender=it.first})
                FormSection("02","Secure your account","Use at least 8 characters. Avoid a common or entirely numeric password.")
                KpnField("Password",password,{password=it},password=true,error=errors["password"])
                KpnField("Confirm password",confirm,{confirm=it},password=true,error=if(confirm.isNotBlank()&&confirm!=password) "Passwords must match." else errors["password_confirm"])
                ErrorState(s.errors["register"]) {vm.clearError("register")}
                KpnPrimaryButton("Continue",s.busy,photo!=null&&first.isNotBlank()&&last.isNotBlank()&&username.isNotBlank()&&phone.isNotBlank()&&email.contains("@")&&gender.isNotBlank()&&password.length>=8&&password==confirm){step=2}
            } else {
                FormSection("03","Your jurisdiction","Choose your location first. Available positions come directly from the backend.")
                ErrorState(s.errors["register_data"] ?: s.errors["locations"]){vm.loadRegistrationData()}
                KpnDropdown("Senatorial zone",zone,s.zones,{it.name},{zone=it;lga=null;ward=null;vm.loadLgas(it.id)})
                KpnDropdown("Local government area",lga,s.lgas,{it.name},{lga=it;ward=null;vm.loadWards(it.id)},enabled=zone!=null)
                KpnDropdown("Ward",ward,s.wards,{it.name},{ward=it},enabled=lga!=null)
                FormSection("04","Available position")
                if(s.roles.isEmpty()) InfoBanner("No vacant positions are available for this location selection. Choose a location or contact KPN. General Member self-registration is not offered by the current website policy.")
                else KpnDropdown("Leadership position",role,s.roles,{it.title},{role=it})
                KpnField("Short biography (optional)",bio,{bio=it},lines=3)
                ErrorState(s.errors["register"]){vm.clearError("register")}
                Text("Submitting an application does not grant leadership access. Your membership remains pending until KPN approves it.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(vertical=16.dp))
                KpnPrimaryButton("Submit application",s.busy,role!=null&&photo!=null){val r=role ?: return@KpnPrimaryButton;vm.register(RegistrationRequest(username.trim(),email.trim(),password,confirm,first.trim(),last.trim(),phone.trim(),gender,zone?.id,lga?.id,ward?.id,r.id,bio),Uri.parse(photo!!))}
            }
        }
    }
}
@Composable internal fun PendingScreen(user:User,onLogout:()->Unit) {
    Column(Modifier.fillMaxSize()) {
        KpnTopBar(if(LocalMemberShell.current)"Overview" else "Membership status")
        Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp)) {
            MemberIdentityPanel(user,user.role_title ?: membershipTierLabel(user.role),user.jurisdictionLabel)
            Spacer(Modifier.height(20.dp));Text(when(user.status){"REJECTED"->"Your application was not approved. Contact KPN for guidance.";"SUSPENDED"->"Your membership is currently suspended. Contact KPN for guidance.";else->"Your application is being reviewed. Operational access will become available when your membership is verified."},style=MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp));Text(user.role_title ?: membershipTierLabel(user.role),style=MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(24.dp));OutlinedButton(modifier=Modifier.heightIn(min=48.dp), onClick=onLogout,shape=MaterialTheme.shapes.small){Text("Sign out")}
        }
    }
}
@Composable internal fun TelegramGateScreen(vm:AppViewModel,roleTitle:String?) {
    val s by vm.state.collectAsStateWithLifecycle();val context=LocalContext.current
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Telegram connection")
        Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp)) {
            Text("Connect your verified membership",style=MaterialTheme.typography.headlineSmall)
            Text(roleTitle.orEmpty(),style=MaterialTheme.typography.titleSmall,modifier=Modifier.padding(vertical=12.dp))
            Text("KPN requires Telegram membership for this position. If you already verified through the website, refresh your account state; you do not need a second identity.",style=MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(20.dp));ErrorState(s.errors["telegram"]){vm.clearError("telegram")}
            KpnPrimaryButton("Open official Telegram connection",s.busy){vm.createTelegramLink{url->runCatching{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))}}}
            OutlinedButton(onClick={vm.boot(force=true)},shape=MaterialTheme.shapes.small,modifier=(Modifier.fillMaxWidth().padding(top=12.dp)).heightIn(min=48.dp)){Text("I’ve verified — refresh account")}
            TextButton(onClick={vm.logout()}){Text("Sign out")}
        }
    }
}
