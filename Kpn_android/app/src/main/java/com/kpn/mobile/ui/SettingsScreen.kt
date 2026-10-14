package com.kpn.mobile.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kpn.mobile.BuildConfig
import com.kpn.mobile.KpnApplication
import com.kpn.mobile.core.AppearanceChoice

@Composable internal fun SettingsScreen(nav:NavHostController) {
    val context=LocalContext.current
    val store=(context.applicationContext as KpnApplication).appearance
    val choice by store.choice.collectAsStateWithLifecycle()
    var launchError by remember {mutableStateOf(false)}
    LazyColumn(contentPadding=PaddingValues(bottom=24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
        item { KpnTopBar("Settings",back={nav.popBackStack()}) }
        item { Column(Modifier.padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("Appearance",style=MaterialTheme.typography.titleLarge)
            Text("Choose how KPN looks on this device. Your choice is saved for your next visit.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
            AppearanceOptions(choice,store::setChoice)
        } }
        item { Column(Modifier.padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("Notifications",style=MaterialTheme.typography.titleLarge)
            Text("Manage notification permission, sound and alerts in Android settings.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
            AccountControlGroup { AccountControl("Device notification settings",Icons.Default.NotificationsNone) {
                launchError=runCatching { context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,context.packageName)) }.isFailure
            } }
            if(launchError)InfoBanner("Unable to open Android settings. Open your device Settings → Apps → KPN → Notifications.",isWarning=true)
        } }
        item { Column(Modifier.padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("About this app",style=MaterialTheme.typography.titleLarge)
            OutlinedCard(shape=MaterialTheme.shapes.medium,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.surface),modifier=Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    Text("Kebbi Progressive Youth Network",style=MaterialTheme.typography.titleMedium)
                    Text("Version ${BuildConfig.VERSION_NAME}",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } }
    }
}
@Composable internal fun AppearanceOptions(choice:AppearanceChoice,onChange:(AppearanceChoice)->Unit) {
    OutlinedCard(shape=MaterialTheme.shapes.medium,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.surface),modifier=Modifier.fillMaxWidth()) {
        Column(Modifier.selectableGroup()) {
            AppearanceChoice.entries.forEachIndexed { index,option ->
                Row(Modifier.fillMaxWidth().selectable(selected=choice==option,role=Role.RadioButton,onClick={onChange(option)}).heightIn(min=64.dp).padding(horizontal=16.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    Icon(when(option){AppearanceChoice.SYSTEM->Icons.Default.BrightnessAuto;AppearanceChoice.LIGHT->Icons.Default.LightMode;AppearanceChoice.DARK->Icons.Default.DarkMode},null,Modifier.size(22.dp),tint=MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text(option.label,style=MaterialTheme.typography.titleMedium)
                        if(option==AppearanceChoice.SYSTEM)Text("Follow your device appearance",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    RadioButton(selected=choice==option,onClick=null)
                }
                if(index<AppearanceChoice.entries.lastIndex)HorizontalDivider(Modifier.padding(start=52.dp),color=MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}
