package com.kpn.mobile.core
/** Do not display an old account's queued push after signing into another account. */
object PushDisplayPolicy {
 fun allows(accessPresent:Boolean,userId:Int?,recipient:String?):Boolean=accessPresent && userId!=null && userId>0 && recipient?.toIntOrNull()==userId
}
