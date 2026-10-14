package com.kpn.mobile.ui

import android.app.NotificationManager
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kpn.mobile.MainActivity
import com.kpn.mobile.R
import com.kpn.mobile.notifications.KpnNotificationChannels
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Local, clearly labelled system-alert QA. No cloud push or real account required. */
@RunWith(AndroidJUnit4::class)
class NativeNotificationChannelTest {
 @Test fun productionAlertChannelSupportsHighImportanceAndSystemDelivery() {
  val instrumentation=InstrumentationRegistry.getInstrumentation()
  val context=instrumentation.targetContext
  val manager=context.getSystemService(NotificationManager::class.java)
  KpnNotificationChannels.ensure(context)
  val channel=requireNotNull(manager.getNotificationChannel(KpnNotificationChannels.ALERTS))
  assertNotNull(channel)
  assertEquals(NotificationManager.IMPORTANCE_HIGH,channel.importance)
  context.startActivity(Intent(context,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
  Thread.sleep(7000)
  val notice=NotificationCompat.Builder(context,KpnNotificationChannels.ALERTS)
   .setSmallIcon(R.drawable.ic_kpn_notification)
   .setContentTitle("Isolated KPN alert test")
   .setContentText("Local system notification QA, not a real announcement.")
   .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).build()
  try {
   manager.notify("qa-alert",90179,notice)
   Thread.sleep(1400)
   assertTrue(manager.activeNotifications.any{it.tag=="qa-alert" && it.id==90179})
   // Screenshot capture is performed externally with adb: UiAutomation can
   // fail to connect on a slow software emulator despite successful delivery.
   Thread.sleep(12000)
  } finally {manager.cancel("qa-alert",90179)}
 }
}
