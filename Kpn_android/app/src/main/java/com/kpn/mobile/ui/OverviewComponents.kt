package com.kpn.mobile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kpn.mobile.core.WorkspaceAction
import com.kpn.mobile.data.UserDto

internal data class ActionTile(val title:String,val route:String,val icon:ImageVector)
internal fun actionIcon(route:String):ImageVector=when {
 route=="members"||route=="mobilization"->Icons.Outlined.Groups
 route.contains("report")->Icons.Outlined.Assignment
 route.contains("article")||route=="editorial"||route=="news_member"->Icons.Outlined.Article
 route.contains("media")->Icons.Outlined.PhotoLibrary
 route.contains("event")||route.contains("meeting")||route=="attendance"->Icons.Outlined.Event
 route.contains("program")||route=="opportunities_member"->Icons.Outlined.WorkOutline
 route.contains("finance")||route.contains("audit")->Icons.Outlined.AccountBalanceWallet
 route=="notifications"||route.contains("announcement")->Icons.Outlined.Notifications
 route=="profile"->Icons.Outlined.AccountCircle
 route=="home"->Icons.Outlined.Home
 route=="dashboard"->Icons.Outlined.Dashboard
 else->Icons.Outlined.FolderOpen
}
internal fun WorkspaceAction.tile()=ActionTile(title,route,actionIcon(route))
/** Only network content without an existing root/account/bell entrance belongs in this group. */
internal val memberUtilityTiles=listOf(ActionTile("Opportunities","opportunities_member",Icons.Outlined.WorkOutline))

/** Compact actions, not data cards: semantic icon, short title, restrained outline, no invented counts. */
@Composable internal fun ActionTileGrid(tiles:List<ActionTile>,onOpen:(String)->Unit) {
 BoxWithConstraints(Modifier.fillMaxWidth()) {
  val columns=if(maxWidth<300.dp||androidx.compose.ui.platform.LocalDensity.current.fontScale>1.4f)1 else 2
  Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
   tiles.chunked(columns).forEach { row ->
    Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
     row.forEach { tile ->
      OutlinedCard(onClick={onOpen(tile.route)},modifier=Modifier.weight(1f).heightIn(min=104.dp),shape=MaterialTheme.shapes.small,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.surface)) {
       Column(Modifier.fillMaxWidth().padding(14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Icon(tile.icon,null,Modifier.size(22.dp),tint=MaterialTheme.colorScheme.primary)
        Text(tile.title,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold)
       }
      }
     }
     repeat(columns-row.size){Spacer(Modifier.weight(1f))}
    }
   }
  }
 }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun MemberIdentityPanel(user:UserDto,role:String,jurisdiction:String) {
 OutlinedCard(Modifier.fillMaxWidth(),shape=MaterialTheme.shapes.small,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.surface)) {
  Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
   Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
    KpnAvatar(user.photo,user.displayName,44)
    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
     Text(user.displayName,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold)
     Text(role,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.primary)
    }
   }
   KpnDivider()
   FlowRow(verticalArrangement=Arrangement.spacedBy(8.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
    Text(jurisdiction,Modifier.fillMaxWidth(),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    StatusBadge(user.status)
   }
  }
 }
}
