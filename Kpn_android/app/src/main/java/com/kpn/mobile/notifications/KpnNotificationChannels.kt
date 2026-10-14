package com.kpn.mobile.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.kpn.mobile.R
import com.kpn.mobile.core.NotificationAlertPolicy

object KpnNotificationChannels {
    const val ALERTS = NotificationAlertPolicy.CHANNEL_ID
    fun ensure(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(ALERTS) != null) return
        val legacy = manager.getNotificationChannel("kpn_updates")
        val userSelected = if (Build.VERSION.SDK_INT >= 29) legacy?.hasUserSetImportance() == true else false
        val importance = NotificationAlertPolicy.importance(legacy?.importance, userSelected)
        val channel = NotificationChannel(ALERTS, context.getString(R.string.notification_alert_channel_name), importance)
        channel.description = context.getString(R.string.notification_alert_channel_description)
        // Never restore a sound or vibration choice that the user disabled.
        if (legacy != null) {
            channel.setSound(legacy.sound, legacy.audioAttributes)
            channel.enableVibration(legacy.shouldVibrate())
            channel.setShowBadge(legacy.canShowBadge())
        }
        manager.createNotificationChannel(channel)
    }
}
