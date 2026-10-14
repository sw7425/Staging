package com.kpn.mobile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

internal data class PublicDirectoryItem(val label:String,val route:String,val icon:ImageVector)
internal data class PublicDirectoryGroup(val title:String,val entries:List<PublicDirectoryItem>)
/** Only secondary destinations: Home, News and Opportunities remain in root navigation. */
internal val publicDirectoryGroups=listOf(
    PublicDirectoryGroup("People & community",listOf(
        PublicDirectoryItem("Leadership","leadership",Icons.Default.Groups),
        PublicDirectoryItem("Patrons","patrons",Icons.Default.VerifiedUser),
        PublicDirectoryItem("Community","community",Icons.Default.LocationCity),
        PublicDirectoryItem("Civic & Advocacy","civic",Icons.Default.Campaign))),
    PublicDirectoryGroup("Organisation",listOf(
        PublicDirectoryItem("About KPN","about",Icons.Default.Info),
        PublicDirectoryItem("Impact","impact",Icons.Default.Insights),
        PublicDirectoryItem("Gallery","gallery",Icons.Default.PhotoLibrary),
        PublicDirectoryItem("Code of Conduct","conduct",Icons.Default.Policy))),
    PublicDirectoryGroup("Help & app",listOf(
        PublicDirectoryItem("FAQ","faq",Icons.Default.HelpOutline),
        PublicDirectoryItem("Contact","contact",Icons.Default.MailOutline),
        PublicDirectoryItem("Support KPN","support",Icons.Default.VolunteerActivism),
        PublicDirectoryItem("Settings","settings",Icons.Default.Settings)))
)
@Composable internal fun PublicDirectoryRow(entries:List<PublicDirectoryItem>,onOpen:(String)->Unit) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        entries.forEach { entry ->
            OutlinedCard(onClick={onOpen(entry.route)},modifier=Modifier.weight(1f).fillMaxHeight().heightIn(min=104.dp),
                shape=MaterialTheme.shapes.medium,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),
                colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    Icon(entry.icon,null,Modifier.size(24.dp),tint=MaterialTheme.colorScheme.primary)
                    Text(entry.label,style=MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}
@Composable internal fun publicDirectoryColumns():Int=if(LocalDensity.current.fontScale>1.3f)1 else 2
