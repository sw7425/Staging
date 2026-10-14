package com.kpn.mobile.core
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
class SessionStore(context:Context):TokenStore{
 private val appContext=context.applicationContext
 private val prefs=try{encrypted()}catch(_:Exception){appContext.deleteSharedPreferences("kpn_secure_session");encrypted()}
 private fun encrypted()=EncryptedSharedPreferences.create(appContext,"kpn_secure_session",MasterKey.Builder(appContext).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
 override fun access():String?=prefs.getString("access",null);override fun refresh():String?=prefs.getString("refresh",null)
 @Synchronized override fun save(access:String,refresh:String?)=prefs.edit().putString("access",access).apply{if(refresh!=null)putString("refresh",refresh)}.apply()
 fun saveDevice(token:String,deviceId:String)=prefs.edit().putString("device_token",token).putString("device_id",deviceId).apply()
 @Synchronized override fun clearTokens(){prefs.edit().remove("access").remove("refresh").apply()}
 @Synchronized fun installationId():String=prefs.getString("device_id",null)?.takeIf{it.isNotBlank()} ?: java.util.UUID.randomUUID().toString().also{prefs.edit().putString("device_id",it).apply()}
 fun saveUserId(id:Int){prefs.edit().putInt("user_id",id).apply()}
 fun userId():Int?=prefs.getInt("user_id",0).takeIf{it>0}
 fun notificationPermissionPrompted():Boolean=prefs.getBoolean("notification_permission_prompted",false)
 fun markNotificationPermissionPrompted(){prefs.edit().putBoolean("notification_permission_prompted",true).apply()}
 fun deviceToken():String?=prefs.getString("device_token",null)
 @Synchronized fun clearDurably(){
  val saved=prefs.edit().remove("access").remove("refresh").remove("device_token").remove("user_id").commit()
  if(!saved) appContext.deleteSharedPreferences("kpn_secure_session")
 }
 @Synchronized fun clear(){prefs.edit().remove("access").remove("refresh").remove("device_token").remove("user_id").apply()}
}
