package com.kpn.mobile.core
import org.junit.Assert.*
import org.junit.Test
class NotificationAlertPolicyTest {
 @Test fun freshInstallUsesHighImportance(){assertEquals(4,NotificationAlertPolicy.importance(null,false))}
 @Test fun oldAppDefaultUpgradesToHeadsUpCapableChannel(){assertEquals(4,NotificationAlertPolicy.importance(3,false))}
 @Test fun explicitUserDefaultOrSilentChoiceIsPreserved(){assertEquals(3,NotificationAlertPolicy.importance(3,true));assertEquals(2,NotificationAlertPolicy.importance(2,true))}
 @Test fun blockedAndLowChannelsAreNeverReenabled(){assertEquals(0,NotificationAlertPolicy.importance(0,false));assertEquals(1,NotificationAlertPolicy.importance(1,false));assertEquals(2,NotificationAlertPolicy.importance(2,false))}
 @Test fun migrationKeepsAStableNewChannelId(){assertEquals("kpn_alerts_v2",NotificationAlertPolicy.CHANNEL_ID);assertNotEquals("kpn_updates",NotificationAlertPolicy.CHANNEL_ID)}
}
