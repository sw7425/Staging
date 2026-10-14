package com.kpn.mobile.core
import org.junit.Assert.*
import org.junit.Test
class PushDisplayPolicyTest {
 @Test fun signedInRecipientCanDisplay(){assertTrue(PushDisplayPolicy.allows(true,42,"42"))}
 @Test fun loggedOutNeverDisplays(){assertFalse(PushDisplayPolicy.allows(false,42,"42"))}
 @Test fun anotherAccountCannotDisplayQueuedPush(){assertFalse(PushDisplayPolicy.allows(true,43,"42"))}
 @Test fun missingOrMalformedRecipientFailsClosed(){assertFalse(PushDisplayPolicy.allows(true,42,null));assertFalse(PushDisplayPolicy.allows(true,42,"wrong"));assertFalse(PushDisplayPolicy.allows(true,null,"42"))}
}
