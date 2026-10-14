package com.kpn.mobile.core
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Test
import org.junit.Assert.*
class NotificationEventsTest {
 @Test fun activeUiReceivesPayloadFreePushSignal()=runBlocking {
  val events=NotificationEvents();val next=async(start=CoroutineStart.UNDISPATCHED){events.updates.first()}
  events.signal();assertEquals(Unit,withTimeout(1000){next.await()})
 }
}
