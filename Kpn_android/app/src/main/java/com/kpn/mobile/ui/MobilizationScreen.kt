@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.kpn.mobile.ui
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kpn.mobile.core.*
import com.kpn.mobile.data.Member
import kotlinx.coroutines.delay

@Composable internal fun MobilizationScreen(vm:AppViewModel,s:AppState,nav:NavHostController) {
    val context=LocalContext.current
    var filters by rememberSaveable{mutableStateOf(hashMapOf("status" to "VERIFIED"))};var draft by remember{mutableStateOf<Map<String,String>>(emptyMap())}
    var search by rememberSaveable{mutableStateOf("")};var filterOpen by remember{mutableStateOf(false)};var exportOpen by remember{mutableStateOf(false)};var viewing by remember{mutableStateOf<Member?>(null)}
    val active=filters+("search" to search)
    val csv=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")){uri->if(uri!=null)vm.exportMobilization("csv",active,uri){android.widget.Toast.makeText(context,"Scoped contact CSV saved",android.widget.Toast.LENGTH_SHORT).show()}}
    val pdf=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")){uri->if(uri!=null)vm.exportMobilization("pdf",active,uri){android.widget.Toast.makeText(context,"Scoped contact PDF saved",android.widget.Toast.LENGTH_SHORT).show()}}
    LaunchedEffect(Unit){vm.loadMobilizationFilters(filters)}
    LaunchedEffect(search,filters){delay(350);vm.loadMobilization(active)}
    if(filterOpen)ModalBottomSheet(sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),onDismissRequest={filterOpen=false}) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Text("Contact filters",style=MaterialTheme.typography.titleLarge);Text("Filters narrow your existing jurisdiction; they never expand it.",style=MaterialTheme.typography.bodyMedium);Spacer(Modifier.height(16.dp))
            s.mobilizationFields.forEach{field->KpnDropdown(field.label,field.choices.firstOrNull{it.value==(draft[field.name] ?: field.default_value ?: "")},field.choices,{it.label},{choice->draft=draft+(field.name to choice.value);if(field.name=="zone"){draft=draft-("lga")-("ward");vm.loadMobilizationFilters(draft)} else if(field.name=="lga"){draft=draft-"ward";vm.loadMobilizationFilters(draft)}})}
            ErrorState(s.errors["mobilization_filters"]){vm.loadMobilizationFilters(draft)}
            TextButton(onClick={draft=mapOf("status" to "VERIFIED");vm.loadMobilizationFilters(draft)}){Text("Reset filters")}
            KpnPrimaryButton("Apply filters",s.busy,s.mobilizationFields.isNotEmpty()){filters=HashMap(draft);filterOpen=false}
            Spacer(Modifier.height(24.dp))
        }
    }
    viewing?.let{member->ModalBottomSheet(sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),onDismissRequest={viewing=null}){Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)){Row(verticalAlignment=Alignment.CenterVertically){KpnAvatar(member.photo,member.name,64);Spacer(Modifier.width(16.dp));Column{Text(member.name,style=MaterialTheme.typography.titleLarge);StatusBadge(member.status)}};Spacer(Modifier.height(16.dp));Text(member.role_title ?: membershipTierLabel(member.role),style=MaterialTheme.typography.titleMedium);Text(listOfNotNull(member.ward_name,member.lga_name,member.zone_name).joinToString(" · "),style=MaterialTheme.typography.bodyMedium);if(member.phone.isNotBlank())OutlinedButton(shape=MaterialTheme.shapes.small,onClick={runCatching{context.startActivity(Intent(Intent.ACTION_DIAL,Uri.fromParts("tel",member.phone,null))) }},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Icon(Icons.Default.Phone,"Open dialer");Spacer(Modifier.width(8.dp));Text(member.phone)};if(member.email.isNotBlank())TextButton(onClick={runCatching{context.startActivity(Intent(Intent.ACTION_SENDTO,Uri.fromParts("mailto",member.email,null)))}}){Text(member.email)};Spacer(Modifier.height(24.dp))}}}
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Mobilization contacts",back={nav.popBackStack()},actions={IconButton(onClick={draft=filters.toMap();vm.loadMobilizationFilters(draft);filterOpen=true}){Icon(Icons.Default.Tune,"Filter contacts")};Box{IconButton(enabled=!s.busy,onClick={exportOpen=true}){Icon(Icons.Default.FileDownload,"Export scoped contacts")};DropdownMenu(expanded=exportOpen,onDismissRequest={exportOpen=false}){DropdownMenuItem(text={Text("Save filtered CSV")},onClick={exportOpen=false;csv.launch("kpn-mobilization-contacts.csv")});DropdownMenuItem(text={Text("Save filtered PDF")},onClick={exportOpen=false;pdf.launch("kpn-mobilization-contacts.pdf")})}}})
        Column(Modifier.padding(horizontal=20.dp)){Text(s.user?.jurisdictionLabel.orEmpty(),style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);KpnField("Search contacts",search,{search=it});ErrorState(s.errors["mobilization"]){vm.loadMobilization(active)};ErrorState(s.errors["mobilization_export"]){vm.clearError("mobilization_export")};Loading(s.busy)}
        LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(20.dp)) {
            if(s.mobilizationMembers.isEmpty()&&!s.busy&&s.errors["mobilization"]==null)item{EmptyState("No contacts match these filters in your authorized scope.")}
            items(s.mobilizationMembers,key={it.id}){member->Column(Modifier.fillMaxWidth().clickable{viewing=member}.padding(vertical=12.dp)){Row(verticalAlignment=Alignment.CenterVertically){KpnAvatar(member.photo,member.name,40);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(member.name,style=MaterialTheme.typography.titleSmall);Text(member.role_title ?: membershipTierLabel(member.role),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);if(member.phone.isNotBlank())Text(member.phone,style=MaterialTheme.typography.bodyMedium)};Icon(Icons.Default.ChevronRight,"View contact")};Spacer(Modifier.height(12.dp));KpnDivider()}}
            if(s.hasMore["mobilization"]==true)item{TextButton(enabled=!s.busy,onClick={vm.loadMobilization(active,true)},modifier=Modifier.fillMaxWidth()){Text("Load more contacts")}}
        }
    }
}
