package com.kpn.mobile.notifications
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kpn.mobile.KpnApplication
import com.kpn.mobile.MainActivity
import com.kpn.mobile.R
import com.kpn.mobile.data.DeviceRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
class KpnFirebaseMessagingService:FirebaseMessagingService(){
 override fun onNewToken(token:String){super.onNewToken(token);val app=application as KpnApplication;val deviceId=app.session.installationId();app.session.saveDevice(token,deviceId);if(app.session.access()!=null) CoroutineScope(Dispatchers.IO).launch{app.api.safe{app.api.api.registerDevice(DeviceRegistration(token=token,device_id=deviceId))}}}
 override fun onMessageReceived(message:RemoteMessage){super.onMessageReceived(message);if(!com.kpn.mobile.core.PushDisplayPolicy.allows((application as KpnApplication).session.access()!=null,(application as KpnApplication).session.userId(),message.data["recipient_user_id"]))return;(application as KpnApplication).notificationEvents.signal();if(Build.VERSION.SDK_INT>=33&&ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;val nm=getSystemService(NotificationManager::class.java);KpnNotificationChannels.ensure(this);val channel=KpnNotificationChannels.ALERTS;val link=message.data["link"]?:message.data["route"]?:"notifications";val intent=Intent(this,MainActivity::class.java).putExtra("deep_link",message.data["deep_link"]).putExtra("link",link).putExtra("event",message.data["event"]).putExtra("notification_id",message.data["notification_id"]).putExtra("target_type",message.data["target_type"]).putExtra("target_id",message.data["target_id"]).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP);val pi=PendingIntent.getActivity(this,(message.data["notification_id"] ?: message.messageId ?: link).hashCode(),intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);val n=NotificationCompat.Builder(this,channel).setSmallIcon(R.drawable.ic_kpn_notification).setContentTitle(message.notification?.title?:message.data["title"]?:"KPN update").setContentText(message.notification?.body?:message.data["message"]?:"").setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).setContentIntent(pi).build();nm.notify(System.currentTimeMillis().toInt(),n)}
}
