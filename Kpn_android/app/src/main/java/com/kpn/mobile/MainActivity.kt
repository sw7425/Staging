package com.kpn.mobile

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.kpn.mobile.core.AppViewModel
import com.kpn.mobile.core.DeepLinkParser
import com.kpn.mobile.ui.KpnApp
import com.kpn.mobile.ui.KpnTheme

class MainActivity:ComponentActivity() {
    private val vm:AppViewModel by viewModels()
    private var resumedBefore=false
    private val permission=registerForActivityResult(ActivityResultContracts.RequestPermission()){}
    private val destination=mutableStateOf<String?>(null)
    private fun consume(intent:Intent?) {
        intent ?: return
        if(intent.data!=null||intent.hasExtra("deep_link")||intent.hasExtra("link")||intent.hasExtra("route")||intent.hasExtra("target_type")) {
            destination.value=DeepLinkParser.fromPayload(listOf("deep_link","link","route","event","notification_id","target_type","target_id").associateWith { intent.getStringExtra(it) } + ("deep_link" to (intent.getStringExtra("deep_link") ?: intent.dataString))).route
        }
    }
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState);consume(intent)
        setContent {
            val account by vm.state.collectAsStateWithLifecycle()
            LaunchedEffect(account.user?.id) {
                val session=(application as KpnApplication).session
                if(Build.VERSION.SDK_INT>=33&&account.user!=null&&!session.notificationPermissionPrompted()&&ContextCompat.checkSelfPermission(this@MainActivity,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){
                    session.markNotificationPermissionPrompted();permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            val appearance by (application as KpnApplication).appearance.choice.collectAsStateWithLifecycle()
            val dark=appearance.isDark(isSystemInDarkTheme())
            KpnTheme(darkTheme=dark) {
                val color=MaterialTheme.colorScheme.background.toArgb()
                SideEffect {
                    @Suppress("DEPRECATION")
                    window.statusBarColor=color
                    @Suppress("DEPRECATION")
                    window.navigationBarColor=color
                    WindowCompat.getInsetsController(window,window.decorView).apply{isAppearanceLightStatusBars=!dark;isAppearanceLightNavigationBars=!dark}
                }
                KpnApp(vm,destination.value) {
                    destination.value=null
                    intent?.data=null
                    listOf("deep_link","link","route","event","notification_id","target_type","target_id").forEach { intent?.removeExtra(it) }
                }
            }
        }
    }
    override fun onResume(){
        super.onResume()
        if(resumedBefore) vm.boot() else resumedBefore=true
    }
    override fun onNewIntent(intent:Intent){super.onNewIntent(intent);setIntent(intent);consume(intent);vm.boot(force=true)}
}
