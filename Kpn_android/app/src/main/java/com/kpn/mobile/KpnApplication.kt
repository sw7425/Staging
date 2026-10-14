package com.kpn.mobile
import android.app.Application
import com.kpn.mobile.core.ApiClient
import com.kpn.mobile.core.SessionStore
class KpnApplication:Application(){ val notificationEvents=com.kpn.mobile.core.NotificationEvents(); lateinit var appearance:com.kpn.mobile.core.AppearanceStore; lateinit var session:SessionStore; lateinit var api:ApiClient; override fun onCreate(){super.onCreate();appearance=com.kpn.mobile.core.AppearanceStore(this);com.kpn.mobile.notifications.KpnNotificationChannels.ensure(this);session=SessionStore(this);api=ApiClient(session)} }
