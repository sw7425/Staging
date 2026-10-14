package com.kpn.mobile.core

/** Preserve explicit user choices while upgrading the app's old default channel. */
object NotificationAlertPolicy {
    const val CHANNEL_ID = "kpn_alerts_v2"
    fun importance(legacyImportance: Int?, userSelectedImportance: Boolean): Int =
        if (legacyImportance != null && (userSelectedImportance || legacyImportance <= 2)) legacyImportance else 4
}
