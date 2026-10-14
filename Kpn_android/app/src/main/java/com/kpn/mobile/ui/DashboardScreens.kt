package com.kpn.mobile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kpn.mobile.core.*

@Composable internal fun DashboardRouter(vm:AppViewModel,s:AppState,nav:NavHostController,policy:RolePolicy) {
 val user=s.user ?: return
 val role=RoleWorkspaces.forKey(user.dashboard)
 LaunchedEffect(user.id,user.dashboard,user.jurisdictionLabel,user.capabilities){vm.loadDashboard()}
 val actions=WorkspaceActions.forUser(user)
 val preferredRoutes=role.blockOrder.flatMap { block -> when(block) {
  "finance"->listOf("finance","financial_reports");"audit"->listOf("audit_reports");"events"->listOf("create_event","events");"discipline"->listOf("discipline");"editorial"->listOf("editorial","compose_article");"community_reports"->listOf("community_report_review","community_report");"faq"->listOf("management");"outreach"->listOf("outreach");"ward_meetings"->listOf("ward_meetings");"reports"->listOf("reports","submit_report");"membership"->listOf("members");else->if(block.startsWith("programs_"))listOf("programs/"+block.removePrefix("programs_")) else emptyList()
 }}
 val primary=preferredRoutes.firstNotNullOfOrNull{route->actions.firstOrNull{it.route==route}}
 val orderedActions=(listOfNotNull(primary)+actions).distinctBy{it.route}
 var expandedOverview by remember(user.id){mutableStateOf(false)}
 var expandedWork by remember(user.id){mutableStateOf(false)}
 val work=s.dashboard?.work_sections.orEmpty().sortedBy{val index=role.blockOrder.indexOf(it.key);if(index<0)99 else index}
 val blocks=s.dashboard?.blocks.orEmpty().sortedBy{val index=role.blockOrder.indexOf(it.key);if(index<0)99 else index}
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(horizontal=20.dp,vertical=16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
  item {
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
    Column(Modifier.weight(1f)) {
     Text("Overview",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.SemiBold)
     Text("Your KPN membership and assigned work",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(top=4.dp))
    }
    IconButton(onClick={vm.loadDashboard(force=true);vm.loadUnreadCount(force=true)}){Icon(Icons.Outlined.Refresh,"Refresh Overview")}
   }
  }
  item{MemberIdentityPanel(user,if(user.tier=="GENERAL")"General Member" else role.title,dashboardJurisdiction(s.dashboard?.jurisdiction,user.jurisdictionLabel))}
  if("dashboard" in s.loading || s.errors["dashboard"]!=null)item{Loading("dashboard" in s.loading);ErrorState(s.errors["dashboard"]){vm.loadDashboard(force=true)}}
  item {
   SectionHeading("Quick actions",action=if(orderedActions.size>4)"All actions" else null,onAction={nav.navigate("workspace")})
   ActionTileGrid(if(orderedActions.isEmpty())memberUtilityTiles else orderedActions.take(4).map{it.tile()}){nav.navigate(it)}
  }
  if(work.isNotEmpty()) {
   items(if(expandedWork)work else work.take(2),key={"work_"+it.key}){section->
    Column {
     KpnDivider()
     SectionHeading(section.title,action="Open",onAction={if(WorkspaceActions.canNavigate(user,section.route))nav.navigate(section.route)})
     if(section.items.isEmpty())Text(section.empty_message,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(vertical=8.dp))
     else section.items.forEach{record->
      DashRow(record.title.take(1),record.title,record.subtitle,record.status.takeIf{it.isNotBlank()},onClick={if(WorkspaceActions.canNavigate(user,record.route))nav.navigate(record.route)})
      KpnDivider()
     }
    }
   }
   if(work.size>2)item{TextButton(onClick={expandedWork=!expandedWork}){Text(if(expandedWork)"Show priority work" else "Show all work queues")}}
  }
  if(user.tier!="GENERAL"&&blocks.isNotEmpty()) {
   item{SectionHeading("Operational summary","Live data for your authorized jurisdiction")}
   items(if(expandedOverview)blocks else blocks.take(3),key={"summary_"+it.key}){block->
    OutlinedCard(Modifier.fillMaxWidth(),shape=MaterialTheme.shapes.small,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.surface)) {
     Column(Modifier.padding(16.dp)) {
      Text(block.title,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(bottom=8.dp))
      if(block.available==false)Text(block.reason ?: "Not available for this jurisdiction.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
      else block.items.forEachIndexed{index,metric->
       Row(Modifier.fillMaxWidth().padding(vertical=10.dp),verticalAlignment=Alignment.CenterVertically) {
        Text(metric.label,Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text(if(metric.currency=="NGN")"₦${metric.value ?: "—"}" else numberText(metric.value),style=MaterialTheme.typography.titleMedium)
       }
       if(index<block.items.lastIndex)KpnDivider()
      }
     }
    }
   }
   if(blocks.size>3)item{TextButton(onClick={expandedOverview=!expandedOverview}){Text(if(expandedOverview)"Show key summaries" else "Show all summaries")}}
  }
  if(user.tier!="GENERAL"&&role.responsibilities.isNotEmpty())item {
   SectionHeading("Role responsibilities")
   role.responsibilities.forEach{Text("• ${roleResponsibility(it,user.lga_name,user.ward_name)}",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(bottom=8.dp))}
  }
 }
}
private fun numberText(value:Any?):String=when(value){is Double->if(value%1.0==0.0)value.toLong().toString() else value.toString();null->"—";else->value.toString()}
@Composable internal fun WorkspaceScreen(vm:AppViewModel,s:AppState,nav:NavHostController) {
 val user=s.user ?: return
 val actions=WorkspaceActions.forUser(user)
 LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
  item{Text("My work",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.SemiBold);Text(s.roleTitle ?: "General Member",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(top=4.dp))}
  actions.groupBy{it.section}.forEach{(section,rows)->item{SectionHeading(section);ActionTileGrid(rows.map{it.tile()}){nav.navigate(it)}}}
  item{SectionHeading("Your network");ActionTileGrid(memberUtilityTiles){nav.navigate(it)}}
 }
}
