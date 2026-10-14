package com.kpn.mobile.core
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
/** Payload-free signal: server remains authoritative for unread and membership state. */
class NotificationEvents {
 private val source=MutableSharedFlow<Unit>(extraBufferCapacity=1)
 val updates=source.asSharedFlow()
 fun signal(){source.tryEmit(Unit)}
}
