package com.kpn.mobile.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kpn.mobile.core.*

@Composable internal fun GeneralMemberDashboard(vm:AppViewModel,s:AppState,nav:NavHostController)=DashboardRouter(vm,s,nav,RolePolicies.fromUser(s.user!!))
@Composable internal fun ProfileScreen(vm:AppViewModel,s:AppState,nav:NavHostController) {
    val user=s.user ?: return;var edit by remember {mutableStateOf(false)}
    var first by remember(user.id){mutableStateOf(user.first_name)};var last by remember(user.id){mutableStateOf(user.last_name)};var email by remember(user.id){mutableStateOf(user.email)};var phone by remember(user.id){mutableStateOf(user.phone)};var bio by remember(user.id){mutableStateOf(user.bio)}
    var gender by remember(user.id){mutableStateOf(user.gender.orEmpty())}
    var facebook by remember(user.id){mutableStateOf(user.facebook_url)};var twitter by remember(user.id){mutableStateOf(user.twitter_url)};var instagram by remember(user.id){mutableStateOf(user.instagram_url)};var tiktok by remember(user.id){mutableStateOf(user.tiktok_url)}
    var photo by remember{mutableStateOf<Uri?>(null)}
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){photo=it}
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("My profile",back={nav.popBackStack()},actions={TextButton(onClick={edit=!edit}){Text(if(edit)"Cancel editing" else "Edit profile")}})
        Column(Modifier.verticalScroll(rememberScrollState()).imePadding().padding(20.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically){KpnImage(photo?.toString() ?: user.photo,user.displayName,Modifier.size(88.dp),portrait=true);Spacer(Modifier.width(16.dp));Column(Modifier.weight(1f)){Text(user.displayName,style=MaterialTheme.typography.titleLarge);Spacer(Modifier.height(8.dp));StatusBadge(user.status)}}
            if(edit) {
                TextButton(onClick={picker.launch("image/*")}){Text("Change profile photo")}
                FormSection("01","Personal details")
                KpnField("First name",first,{first=it},error=s.errors["profile"]?.fields?.get("first_name"));KpnField("Last name",last,{last=it});KpnField("Email address",email,{email=it},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email));KpnField("Phone number",phone,{phone=it},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Phone));KpnDropdown("Gender",gender,listOf("","M","F"),{when(it){"M"->"Male";"F"->"Female";else->"Not specified"}},{gender=it});KpnField("Biography",bio,{bio=it},lines=4)
                FormSection("02","Social profiles","Optional links, shared with your KPN profile settings.")
                KpnField("Facebook URL",facebook,{facebook=it},error=s.errors["profile"]?.fields?.get("facebook_url"));KpnField("X / Twitter URL",twitter,{twitter=it},error=s.errors["profile"]?.fields?.get("twitter_url"));KpnField("Instagram URL",instagram,{instagram=it},error=s.errors["profile"]?.fields?.get("instagram_url"));KpnField("TikTok URL",tiktok,{tiktok=it},error=s.errors["profile"]?.fields?.get("tiktok_url"))
                ErrorState(s.errors["profile"]){vm.clearError("profile")}
                KpnPrimaryButton("Save profile",s.busy){vm.saveProfile(mapOf("first_name" to first,"last_name" to last,"email" to email,"phone" to phone,"bio" to bio,"gender" to gender,"facebook_url" to facebook,"twitter_url" to twitter,"instagram_url" to instagram,"tiktok_url" to tiktok),photo){edit=false;photo=null}}
            } else {
                SectionHeading("Membership")
                listOf("Role" to (user.role_title ?: "General Member"),"Username" to user.username,"Jurisdiction" to user.jurisdictionLabel,"Location" to listOfNotNull(user.ward_name,user.lga_name,user.zone_name).joinToString(" · ").ifBlank{"Not assigned"},"Reporter level" to user.reporter_level.replace('_',' '),"Email" to user.email,"Phone" to user.phone).forEach{(label,value)->Text(label,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(top=12.dp));Text(value,style=MaterialTheme.typography.bodyMedium);Spacer(Modifier.height(12.dp));KpnDivider()}
                if(user.bio.isNotBlank()){SectionHeading("Biography");ReadableBody(user.bio)}
                SectionHeading("Connected account")
                Text(if(user.access.telegram_verified) "Telegram membership verified" else if(user.telegram_membership!=null)"Telegram connected; membership not verified" else "Telegram is not connected",style=MaterialTheme.typography.bodyMedium)
                TextButton(onClick={vm.boot(force=true)}){Icon(Icons.Default.Refresh,"Refresh membership");Spacer(Modifier.width(8.dp));Text("Refresh account state")}
                ActionRow("Change password","Use your current password to secure your account"){nav.navigate("change_password")}
            }
        }
    }
}
@Composable internal fun NotificationsScreen(vm:AppViewModel,s:AppState,nav:NavHostController) {
    LaunchedEffect(Unit){vm.loadNotifications()};var unreadOnly by remember{mutableStateOf(false)}
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Notifications",back={nav.popBackStack()},actions={IconButton(onClick={vm.loadNotifications(force=true)}){Icon(Icons.Default.Refresh,"Refresh notifications")}})
        Row(Modifier.padding(horizontal=20.dp),verticalAlignment=Alignment.CenterVertically){FilterChip(selected=unreadOnly,onClick={unreadOnly=!unreadOnly},label={Text("Unread${s.unreadCount?.let{" ($it)"}.orEmpty()}")});Spacer(Modifier.weight(1f));TextButton(onClick={vm.readAll()},enabled=!("notifications" in s.loading)&&s.notifications.any{!it.is_read}){Text("Mark all read")}}
        Loading("notifications" in s.loading)
        ErrorState(s.errors["notifications"]){vm.loadNotifications(force=true)}
        LazyColumn(contentPadding=PaddingValues(horizontal=20.dp,vertical=8.dp)) {
            val rows=if(unreadOnly)s.notifications.filter{!it.is_read} else s.notifications
            if(rows.isEmpty()&&!("notifications" in s.loading)&&s.errors["notifications"]==null)item{EmptyState(if(unreadOnly)"No unread notifications in this page." else "No notifications yet.")}
            items(rows,key={it.id}){n->
                Column(Modifier.fillMaxWidth().padding(vertical=12.dp)) {
                    Row(Modifier.fillMaxWidth().heightIn(min=48.dp).clickable{vm.readNotification(n.id);val link=DeepLinkParser.parse(null,n.link,n.event,n.id.toString(),n.target_type,n.target_id).route;if(link!="notifications"&&WorkspaceActions.canNavigate(s.user!!,link))nav.navigate(link)}) {
                        Icon(if(n.is_read)Icons.Default.NotificationsNone else Icons.Default.NotificationsActive,if(n.is_read)"Read notification" else "Unread notification",tint=if(n.is_read)MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,modifier=Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(n.title,style=MaterialTheme.typography.titleSmall);Text(n.message,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);n.created_at?.let{Text(KpnDates.dateTime(it),style=MaterialTheme.typography.labelSmall,modifier=Modifier.padding(top=6.dp))}}
                    }
                    TextButton(onClick={vm.notificationState(n.id,!n.is_read)},modifier=Modifier.align(Alignment.End)){Text(if(n.is_read)"Mark unread" else "Mark read")};KpnDivider()
                }
            }
            if(s.hasMore["notifications"]==true)item{TextButton(onClick={vm.loadNotifications(true)},enabled=!("notifications" in s.loading),modifier=Modifier.fillMaxWidth()){Text("Load more notifications")}}
        }
    }
}
@Composable internal fun MoreScreen(vm:AppViewModel,s:AppState,nav:NavHostController,policy:RolePolicy) {
    var confirmLogout by remember{mutableStateOf(false)}
    if(confirmLogout)AlertDialog(onDismissRequest={confirmLogout=false},title={Text("Sign out of KPN?")},text={Text("Your encrypted access and refresh tokens will be removed from this device.")},confirmButton={TextButton(onClick={vm.logout();confirmLogout=false}){Text("Sign out")}},dismissButton={TextButton(onClick={confirmLogout=false}){Text("Cancel")}})
    var showNetwork by remember { mutableStateOf(false) }
    val user=s.user ?: return
    LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
        item { Column(verticalArrangement=Arrangement.spacedBy(4.dp)) { Text("Account",style=MaterialTheme.typography.headlineSmall);Text("Your membership and account settings",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant) } }
        item { AccountIdentity(user){nav.navigate("profile")} }
        item { Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("My account",style=MaterialTheme.typography.titleMedium)
            AccountControlGroup {
                AccountControl("Notifications",Icons.Default.NotificationsNone,s.unreadCount?.takeIf{it>0}?.let{"$it unread"}){nav.navigate("notifications")}
                HorizontalDivider(Modifier.padding(start=52.dp),color=MaterialTheme.colorScheme.outlineVariant)
                AccountControl("Password & security",Icons.Default.Lock){nav.navigate("change_password")}
                HorizontalDivider(Modifier.padding(start=52.dp),color=MaterialTheme.colorScheme.outlineVariant)
                AccountControl("Received announcements",Icons.Default.Campaign){nav.navigate("received_announcements")}
                HorizontalDivider(Modifier.padding(start=52.dp),color=MaterialTheme.colorScheme.outlineVariant)
                AccountControl("Settings",Icons.Default.Settings){nav.navigate("settings")}
            }
        } }
        item { Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("KPN network",style=MaterialTheme.typography.titleMedium)
            AccountControlGroup {
                AccountControl(if(showNetwork) "Close KPN links" else "Network links",Icons.Default.Public,trailing=if(showNetwork)Icons.Default.ExpandLess else Icons.Default.ExpandMore){showNetwork=!showNetwork}
                if(showNetwork) {
                    HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
                    listOf("Leadership" to "leadership","Patrons" to "patrons","Community" to "community","Civic & advocacy" to "civic","Impact" to "impact","About KPN" to "about","Code of conduct" to "conduct","Gallery" to "gallery","FAQs" to "faq","Support KPN" to "support","Contact" to "contact").forEach { (label,route) ->
                        AccountControl(label,homePillarIcon(route)){nav.navigate(route)}
                    }
                }
            }
        } }
        item { Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick={confirmLogout=true},shape=MaterialTheme.shapes.small,colors=ButtonDefaults.outlinedButtonColors(contentColor=MaterialTheme.colorScheme.error),modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)) { Icon(Icons.Default.Logout,null,Modifier.size(20.dp));Spacer(Modifier.width(8.dp));Text("Sign out") }
            Text("KPN · ${com.kpn.mobile.BuildConfig.VERSION_NAME}",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        } }
    }
}
