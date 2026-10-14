package com.kpn.mobile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.kpn.mobile.data.HomeContent
import com.kpn.mobile.data.HomePillar
import com.kpn.mobile.data.UserDto

/** Account identity is separate from operational dashboard summaries. All values are real session data. */
@Composable internal fun AccountIdentity(user: UserDto, onProfile: () -> Unit) {
    OutlinedCard(shape=MaterialTheme.shapes.large,
        border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),
        colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.surface),
        modifier=Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(4.dp).background(KpnPalette.Brand))
        Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                KpnAvatar(user.photo,user.displayName,64)
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    Text(user.displayName,style=MaterialTheme.typography.titleLarge)
                    Text("@${user.username}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(user.role_title ?: if(user.role=="GENERAL") "General Member" else user.role,
                        style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary)
                }
            }
            HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
            Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.LocationOn,null,Modifier.size(18.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(user.jurisdictionLabel,style=MaterialTheme.typography.bodySmall,modifier=Modifier.weight(1f))
                }
                StatusBadge(user.status)
            }
            OutlinedButton(onClick=onProfile,shape=MaterialTheme.shapes.small,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)) {
                Icon(Icons.Default.ManageAccounts,null,Modifier.size(20.dp));Spacer(Modifier.width(8.dp));Text("Manage profile")
            }
        }
    }
}

@Composable internal fun AccountControl(title:String, icon:ImageVector, value:String?=null, trailing:ImageVector=Icons.AutoMirrored.Filled.ArrowForward, onClick:()->Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick=onClick).heightIn(min=64.dp).padding(horizontal=16.dp,vertical=16.dp),
        verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
        Icon(icon,null,Modifier.size(22.dp),tint=MaterialTheme.colorScheme.primary)
        Text(title,style=MaterialTheme.typography.titleSmall,modifier=Modifier.weight(1f))
        if(value!=null)Surface(color=MaterialTheme.colorScheme.primaryContainer,shape=MaterialTheme.shapes.extraSmall) {
            Text(value,Modifier.padding(horizontal=8.dp,vertical=4.dp),style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Icon(trailing,null,Modifier.size(18.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable internal fun AccountControlGroup(content:@Composable ColumnScope.()->Unit) {
    OutlinedCard(shape=MaterialTheme.shapes.medium,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),
        colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.surface),modifier=Modifier.fillMaxWidth()) {
        Column(content=content)
    }
}

/** Brand-led native front door; official narrative is passed through unchanged. */
@Composable internal fun HomeIntroduction(home:HomeContent,member:Boolean,onJoin:()->Unit,onNews:()->Unit) {
    val darkHero = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val shade = Color.Black.copy(alpha = if (darkHero) 0.48f else 0.22f)
    val heroGradient = Brush.linearGradient(listOf(KpnPalette.Brand, Color(0xFF15803D), KpnPalette.Blue).map { shade.compositeOver(it) })
    val heroColors = MaterialTheme.colorScheme.copy(
        primary = Color.White, onPrimary = KpnPalette.Action,
        primaryContainer = Color.Black.copy(alpha = 0.12f), onPrimaryContainer = Color.White,
        onSurface = Color.White, onSurfaceVariant = Color.White,
        outline = Color.White, outlineVariant = Color.White.copy(alpha = 0.35f),
    )
    MaterialTheme(colorScheme = heroColors) {
    Surface(color=Color.Transparent, contentColor=Color.White) {
        Column(Modifier.fillMaxWidth().background(heroGradient).padding(horizontal=20.dp,vertical=24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                Box(Modifier.width(28.dp).height(3.dp).background(Color.White))
                Text("KEBBI STATE · NIGERIA",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.primary)
            }
            Text(home.organization,style=MaterialTheme.typography.displaySmall)
            Surface(color=MaterialTheme.colorScheme.primaryContainer,shape=MaterialTheme.shapes.small) {
                Text("\"${home.motto}\"",Modifier.padding(horizontal=12.dp,vertical=8.dp),style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Text(home.introduction,style=MaterialTheme.typography.bodyLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)
            if(!member)KpnPrimaryButton("Join the Network",false,onClick=onJoin)
            if(member)OutlinedButton(onClick=onNews,shape=MaterialTheme.shapes.small,modifier=Modifier.heightIn(min=48.dp)) {
                Text("Explore the Newsroom");Spacer(Modifier.width(12.dp));Icon(Icons.AutoMirrored.Filled.ArrowForward,null,Modifier.size(18.dp))
            } else TextButton(onClick=onNews,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text("Explore the Newsroom")}
        }
    }
}
}

internal fun homePillarIcon(route:String):ImageVector=when(route) {
    "news","news_member"->Icons.Default.Newspaper
    "opportunities","opportunities_member"->Icons.Default.WorkOutline
    "community"->Icons.Default.Groups
    "civic","advocacy"->Icons.Default.Campaign
    else->Icons.Default.Public
}
@Composable internal fun HomePillars(pillars:List<HomePillar>,onOpen:(String)->Unit) {
    val columns=if(LocalDensity.current.fontScale>1.3f)1 else 2
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        pillars.chunked(columns).forEach { group ->
            Row(Modifier.height(IntrinsicSize.Min),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                group.forEach { pillar ->
                    OutlinedCard(onClick={onOpen(pillar.route)},modifier=Modifier.weight(1f).fillMaxHeight(),shape=MaterialTheme.shapes.medium,
                        border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),
                        colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                                Icon(homePillarIcon(pillar.route),null,Modifier.size(24.dp),tint=MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.weight(1f));Icon(Icons.AutoMirrored.Filled.ArrowForward,null,Modifier.size(16.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(pillar.title,style=MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}
