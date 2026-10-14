@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.kpn.mobile.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kpn.mobile.core.*
import com.kpn.mobile.data.*

@Composable internal fun ManagementHomeScreen(vm:AppViewModel,s:AppState,nav:NavHostController) {
    LaunchedEffect(Unit){vm.loadResourceCatalog()}
    LazyColumn(contentPadding=PaddingValues(20.dp)) {
        item{KpnTopBar("Content & administration",back={nav.popBackStack()});ErrorState(s.errors["resources"]){vm.loadResourceCatalog()}}
        items(s.resourceCatalog,key={it.kind}){spec->ActionRow(spec.title,"Create, edit and maintain authoritative KPN records"){nav.navigate("resource/${spec.kind}")}}
        if(s.resourceCatalog.isEmpty()&&!s.busy&&s.errors["resources"]==null)item{EmptyState("No editable resources are assigned to your role.")}
    }
}
@Composable internal fun ResourceScreen(vm:AppViewModel,s:AppState,nav:NavHostController,kind:String) {
    LaunchedEffect(kind){vm.loadResourceCatalog();vm.loadResources(kind)}
    val spec=s.resourceCatalog.firstOrNull{it.kind==kind}
    val unsupportedWardProgramme=kind.startsWith("programs_")&&s.user?.tier=="WARD"
    var editing by remember(kind){mutableStateOf(false)};var record by remember(kind){mutableStateOf<Map<String,Any?>?>(null)}
    var deleting by remember{mutableStateOf<Map<String,Any?>?>(null)}
    deleting?.let{row->AlertDialog(onDismissRequest={deleting=null},title={Text("Delete this record?")},text={Text("This removes the record from the website and Android application. This cannot be undone from the app.")},confirmButton={TextButton(onClick={vm.deleteResource(kind,recordId(row)){deleting=null}}){Text("Delete")}},dismissButton={TextButton(onClick={deleting=null}){Text("Cancel")}})}
    if(editing&&spec!=null) {
        ResourceEditor(vm,s,spec,record,onCancel={editing=false}) {editing=false;vm.loadResources(kind)}
    } else Column(Modifier.fillMaxSize()) {
        KpnTopBar(spec?.title ?: "KPN records",back={nav.popBackStack()},actions={if(spec!=null&&!unsupportedWardProgramme)IconButton(onClick={record=null;editing=true}){Icon(Icons.Default.Add,"Create record")}})
        Loading(s.busy);ErrorState(s.errors["resources"]){vm.loadResources(kind)}
        if(unsupportedWardProgramme)InfoBanner("Ward-specific programme management is not configured in the website data model. Contact the Member Support & Welfare director; the app will not grant LGA-wide or State-wide programme access.")
        LazyColumn(contentPadding=PaddingValues(20.dp)) {
            if(s.resourceRows.isEmpty()&&!s.busy&&s.errors["resources"]==null)item{EmptyState(if(unsupportedWardProgramme)"No configured Ward programme records." else "No records yet. Use the add action to create one.")}
            items(s.resourceRows,key={recordId(it)}){row->
                Column(Modifier.fillMaxWidth().padding(vertical=12.dp)) {
                    Text(recordTitle(row),style=MaterialTheme.typography.titleMedium)
                    row["status"]?.let{StatusBadge(it.toString())}
                    val summary=(row["description"] ?: row["summary"] ?: row["question"] ?: row["title"] ?: row["purpose"] ?: row["bio"]).toString().takeUnless{it=="null"}.orEmpty()
                    if(summary.isNotBlank())Text(summary,style=MaterialTheme.typography.bodyMedium,maxLines=3)
                    Row {if(spec?.can_edit==true&&(kind!="audit-reports"||row["status"]=="DRAFT"))TextButton(onClick={record=row;editing=true}){Text("View / edit")};Spacer(Modifier.weight(1f));if(spec?.can_delete==true)TextButton(onClick={deleting=row}){Text("Delete",color=MaterialTheme.colorScheme.error)}}
                    KpnDivider()
                }
            }
        }
    }
}
private fun recordId(row:Map<String,Any?>)=(row["id"] as? Number)?.toInt() ?: row["id"].toString().toIntOrNull() ?: 0
private fun recordTitle(row:Map<String,Any?>)=(row["title"] ?: row["full_name"] ?: row["question"] ?: row["organization"] ?: row["donor_name"] ?: row["description"] ?: "KPN record").toString()
private fun fieldText(value:Any?):String=when(value){is Double->if(value%1.0==0.0)value.toInt().toString() else value.toString();null->"";else->value.toString()}
@Composable private fun ResourceEditor(vm:AppViewModel,s:AppState,spec:ResourceSpec,record:Map<String,Any?>?,onCancel:()->Unit,onSaved:()->Unit) {
    var values by remember(spec.kind,record){mutableStateOf(spec.fields.filter{it.type !in listOf("image","file")}.associate{it.name to if(record==null)it.default_value.orEmpty() else fieldText(record[it.name])})}
    var files by remember{mutableStateOf<Map<String,Uri>>(emptyMap())};var pickingField by remember{mutableStateOf<String?>(null)}
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->val key=pickingField;if(uri!=null&&key!=null)files=files+(key to uri)}
    Column(Modifier.fillMaxSize()) {
        KpnTopBar(if(record==null)"Create ${spec.title.lowercase()}" else "Edit record",back=onCancel)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().padding(20.dp)) {
            Text("Changes update the same records used by the KPN website.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            FormSection("01",spec.title)
            spec.fields.forEach{field->
                val label=field.label+if(field.required)" *" else ""
                when(field.type) {
                    "choice"->KpnDropdown(label,field.choices.firstOrNull{it.value==values[field.name]},field.choices,{it.label},{values=values+(field.name to it.value)})
                    "boolean"->Row(Modifier.fillMaxWidth().padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically){Text(label,Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium);Switch(checked=values[field.name]=="true",onCheckedChange={values=values+(field.name to it.toString())})}
                    "image","file"->{Text(label,style=MaterialTheme.typography.titleSmall);if(field.type=="image")KpnImage(files[field.name]?.toString() ?: record?.get(field.name)?.toString(),label,Modifier.fillMaxWidth().height(150.dp),portrait=true);TextButton(onClick={pickingField=field.name;picker.launch(if(field.type=="image")"image/*" else "*/*")}){Text(if(files.containsKey(field.name))"Replace selected file" else "Choose ${field.type}")}}
                    "date","datetime"->KpnDateField(label,values[field.name].orEmpty(),{values=values+(field.name to it)},dateTime=field.type=="datetime",error=s.errors["resources"]?.fields?.get(field.name))
                    "time"->KpnTimeField(label,values[field.name].orEmpty(),{values=values+(field.name to it)},error=s.errors["resources"]?.fields?.get(field.name))
                    else->KpnField(label,values[field.name].orEmpty(),{values=values+(field.name to it)},keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=when(field.type){"integer"->androidx.compose.ui.text.input.KeyboardType.Number;"number"->androidx.compose.ui.text.input.KeyboardType.Decimal;"email"->androidx.compose.ui.text.input.KeyboardType.Email;"url"->androidx.compose.ui.text.input.KeyboardType.Uri;else->androidx.compose.ui.text.input.KeyboardType.Text}),lines=if(field.name in setOf("content","description","bio","summary","findings","recommendations","answer","purpose","notes","background","kpn_position","actions_taken","government_response","outcome"))4 else 1,error=s.errors["resources"]?.fields?.get(field.name))
                }
            }
            ErrorState(s.errors["resources"]){vm.clearError("resources")}
            val complete=spec.fields.filter{it.required}.all{field->if(field.type in listOf("image","file"))files.containsKey(field.name)||record?.get(field.name)!=null else if(field.type=="boolean")true else !values[field.name].isNullOrBlank()}
            KpnPrimaryButton("Save record",s.busy,complete){vm.saveResource(spec.kind,record?.let(::recordId),values,files,onSaved)}
            Spacer(Modifier.height(24.dp))
        }
    }
}
@Composable internal fun ProgramCreateScreen(vm:AppViewModel,s:AppState,nav:NavHostController,kind:String)=ResourceScreen(vm,s,nav,"programs_$kind")

@Composable internal fun EventDetailScreen(vm:AppViewModel,s:AppState,nav:NavHostController,id:Int) {
    LaunchedEffect(id){vm.loadEvent(id);vm.loadMinutes(id)}
    val event=s.selectedEvent?.takeIf{it.id==id};val caps=s.user?.capabilities.orEmpty()
    var editMinutes by remember{mutableStateOf(false)};var content by remember(s.minutes?.id){mutableStateOf(s.minutes?.content.orEmpty())};var summary by remember(s.minutes?.id){mutableStateOf(s.minutes?.summary.orEmpty())};var published by remember(s.minutes?.id){mutableStateOf(s.minutes?.is_published==true)}
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Event details",back={nav.popBackStack()});ErrorState(s.errors["event_detail"]){vm.loadEvent(id)}
        event?.let{e->Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
            Text(e.title,style=MaterialTheme.typography.headlineSmall);Text(e.location,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);Text("${KpnDates.dateTime(e.start_date)} — ${KpnDates.dateTime(e.end_date)}",style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(vertical=12.dp));ReadableBody(e.description)
            if("RECORD_ATTENDANCE" in caps)ActionRow("Record attendance","Mark members present or absent for this event"){vm.selectEvent(e);nav.navigate("attendance")}
            if("MANAGE_EVENTS" in caps)ActionRow("Edit event","Update the venue, dates and event details"){nav.navigate("resource/events")}
            SectionHeading("Meeting minutes")
            if(editMinutes&&"EDIT_MINUTES" in caps) {
                KpnField("Summary",summary,{summary=it},lines=3);KpnField("Minutes",content,{content=it},lines=7)
                Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=published,onCheckedChange={published=it});Text("Publish minutes")}
                ErrorState(s.errors["minutes"]){vm.clearError("minutes")}
                KpnPrimaryButton("Save minutes",s.busy,content.isNotBlank()){vm.saveMinutes(id,MeetingMinutes(content=content,summary=summary,is_published=published,attendees_present=s.minutes?.attendees_present.orEmpty())){editMinutes=false}}
            } else {
                s.minutes?.let{m->StatusBadge(if(m.is_published)"PUBLISHED" else "DRAFT");if(m.summary.isNotBlank())Text(m.summary,style=MaterialTheme.typography.bodyMedium);Spacer(Modifier.height(12.dp));ReadableBody(m.content)} ?: Text("No minutes have been recorded.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
                if("EDIT_MINUTES" in caps)TextButton(onClick={editMinutes=true}){Text("Edit minutes")}
            }
        }}
    }
}
@Composable internal fun MediaUploadScreen(vm:AppViewModel,s:AppState,nav:NavHostController) {
    var file by remember{mutableStateOf<Uri?>(null)};var title by remember{mutableStateOf("")};var description by remember{mutableStateOf("")};var type by remember{mutableStateOf("PHOTO")}
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){file=it}
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Upload media",back={nav.popBackStack()})
        Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
            FormSection("01","Media submission","Uploads enter the existing KPN review pipeline.")
            KpnDropdown("Media type",type,listOf("PHOTO","VIDEO"),{it.lowercase().replaceFirstChar{it.uppercase()}},{type=it;file=null})
            OutlinedButton(modifier=Modifier.heightIn(min=48.dp), onClick={picker.launch(if(type=="PHOTO")"image/*" else "video/*")},shape=MaterialTheme.shapes.small){Text(if(file==null)"Choose file" else "Change selected file")}
            if(type=="PHOTO"&&file!=null)KpnImage(file.toString(),"Selected media",Modifier.fillMaxWidth().height(180.dp))
            KpnField("Title",title,{title=it});KpnField("Description",description,{description=it},lines=4);ErrorState(s.errors["media_upload"]){vm.clearError("media_upload")}
            KpnPrimaryButton("Submit media",s.busy,file!=null&&title.isNotBlank()){vm.uploadMedia(file!!,title,description,type){nav.popBackStack()}}
        }
    }
}
@Composable internal fun MyArticlesScreen(vm:AppViewModel,s:AppState,nav:NavHostController) {
    LaunchedEffect(Unit){vm.loadMyArticles()};var deleting by remember{mutableStateOf<Article?>(null)};var viewing by remember{mutableStateOf<Article?>(null)}
    deleting?.let{article->AlertDialog(onDismissRequest={deleting=null},title={Text("Delete draft?")},text={Text(article.title)},confirmButton={TextButton(onClick={vm.deleteDraft(article.id){deleting=null}}){Text("Delete")}},dismissButton={TextButton(onClick={deleting=null}){Text("Cancel")}})}
    viewing?.let{article->ModalBottomSheet(sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),onDismissRequest={viewing=null}){Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)){Text(article.title,style=MaterialTheme.typography.headlineSmall);article.status?.let{StatusBadge(it)};if(article.subheadline.isNotBlank()){Spacer(Modifier.height(12.dp));Text(article.subheadline,style=MaterialTheme.typography.titleMedium)};if(article.rejection_note?.isNotBlank()==true)InfoBanner(article.rejection_note,isWarning=true);val image=article.featured_image ?: article.image;if(image!=null){Spacer(Modifier.height(16.dp));KpnImage(image,"Article featured image",Modifier.fillMaxWidth().aspectRatio(16f/9f))};Spacer(Modifier.height(16.dp));ArticleBody(article);Spacer(Modifier.height(24.dp))}}}

    Column(Modifier.fillMaxSize()) {
        KpnTopBar("My articles",back={nav.popBackStack()},actions={IconButton(onClick={nav.navigate("compose_article")}){Icon(Icons.Default.Add,"Create article")}})
        ErrorState(s.errors["articles"]){vm.loadMyArticles()};Loading(s.busy)
        LazyColumn(contentPadding=PaddingValues(20.dp)) {
            if(s.articles.isEmpty()&&!s.busy&&s.errors["articles"]==null)item{EmptyState("No article drafts yet.")}
            items(s.articles,key={it.id}){a->Column(Modifier.padding(vertical=12.dp)){Text(a.title,style=MaterialTheme.typography.titleMedium);a.status?.let{StatusBadge(it)};TextButton(onClick={viewing=a}){Text("Read article")};if(a.rejection_note?.isNotBlank()==true)InfoBanner(a.rejection_note,isWarning=true);if(a.status in listOf("DRAFT","REJECTED")){Row{TextButton(onClick={nav.navigate("edit_article/${a.id}")}){Text(if(a.status=="REJECTED")"Edit & resubmit" else "Edit")};if(a.status=="DRAFT")TextButton(onClick={vm.submitArticleForReview(a.id){if(it)vm.loadMyArticles()}}){Text("Submit for review")};TextButton(onClick={deleting=a}){Text("Delete")}}};KpnDivider()}}
        }
    }
}
@Composable internal fun ReceivedAnnouncementsScreen(vm:AppViewModel,s:AppState,nav:NavHostController) {
    LaunchedEffect(Unit){vm.loadReceivedAnnouncements()}
    LazyColumn(contentPadding=PaddingValues(20.dp)) {
        item{KpnTopBar("Received announcements",back={nav.popBackStack()});ErrorState(s.errors["receivedAnnouncements"]){vm.loadReceivedAnnouncements()}}
        if(s.receivedAnnouncements.isEmpty()&&!s.busy&&s.errors["receivedAnnouncements"]==null)item{EmptyState("No active notices for your membership.")}
        items(s.receivedAnnouncements,key={it.id}){a->Column(Modifier.padding(vertical=12.dp)){Text(a.title,style=MaterialTheme.typography.titleMedium);StatusBadge(a.priority);Spacer(Modifier.height(10.dp));ReadableBody(a.content);Spacer(Modifier.height(16.dp));KpnDivider()}}
    }
}

@Composable internal fun ParticipantEditor(vm:AppViewModel,s:AppState,kind:String,id:Int,title:String,onClose:()->Unit) {
    var changes by remember(id){mutableStateOf<Map<Int,Boolean>>(emptyMap())}
    ModalBottomSheet(sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),onDismissRequest=onClose) {
        Column(Modifier.fillMaxWidth().heightIn(max=600.dp).padding(20.dp)) {
            Text(title,style=MaterialTheme.typography.titleLarge);Text("Programme participants",style=MaterialTheme.typography.bodyMedium)
            ErrorState(s.errors["participants"]){vm.loadProgramParticipants(kind,id)}
            LazyColumn(Modifier.weight(1f,false)) {items(s.members,key={it.id}){m->Row(verticalAlignment=Alignment.CenterVertically){Text(m.name,Modifier.weight(1f));Checkbox(checked=changes[m.id] ?: (m.id in s.participantIds),onCheckedChange={changes=changes+(m.id to it)})}}}
            KpnPrimaryButton("Save participants",s.busy,changes.isNotEmpty()&&s.errors["participants"]==null){val ids=s.participantIds.toMutableSet();changes.forEach{(member,include)->if(include)ids.add(member) else ids.remove(member)};vm.saveProgramParticipants(kind,id,ids.toList(),onClose)}
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable internal fun WardAttendanceEditor(vm:AppViewModel,s:AppState,meeting:WardMeeting,onClose:()->Unit) {
    LaunchedEffect(meeting.id){vm.loadMembers(false);vm.loadWardAttendance(meeting.id)}
    var changes by remember(meeting.id){mutableStateOf<Map<Int,Boolean>>(emptyMap())}
    ModalBottomSheet(sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),onDismissRequest=onClose) {
        Column(Modifier.fillMaxWidth().heightIn(max=600.dp).padding(20.dp)) {
            Text(meeting.title,style=MaterialTheme.typography.titleLarge);Text("Ward attendance",style=MaterialTheme.typography.bodyMedium)
            ErrorState(s.errors["attendance"]){vm.loadWardAttendance(meeting.id)}
            LazyColumn(Modifier.weight(1f,false)){items(s.members.filter{it.ward==meeting.ward},key={it.id}){m->Row(verticalAlignment=Alignment.CenterVertically){Text(m.name,Modifier.weight(1f));Checkbox(checked=changes[m.id] ?: s.attendance.firstOrNull{it.attendee==m.id}?.present ?: false,onCheckedChange={changes=changes+(m.id to it)})}}}
            KpnPrimaryButton("Save attendance",s.busy,changes.isNotEmpty()&&s.errors["attendance"]==null){vm.saveWardAttendance(meeting.id,changes.map{(member,present)->AttendanceRecord(attendee=member,present=present)},onClose)}
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable internal fun DisciplineCreateScreen(vm:AppViewModel,s:AppState,onBack:()->Unit) {
    LaunchedEffect(Unit){vm.loadMembers(false)}
    var member by remember{mutableStateOf<Member?>(null)};var type by remember{mutableStateOf("WARNING")};var reason by remember{mutableStateOf("")};var confirm by remember{mutableStateOf(false)}
    if(confirm)AlertDialog(onDismissRequest={confirm=false},title={Text("Submit disciplinary action?")},text={Text(if(type=="WARNING")"A warning is immediately approved by the backend. Review the member and reason carefully." else "This action enters the existing approval workflow. Dismissal also requires a favorable legal opinion.")},confirmButton={TextButton(enabled=!s.busy,onClick={vm.createDiscipline(member!!.id,type,reason){confirm=false;onBack()}}){Text("Submit")}},dismissButton={TextButton(onClick={confirm=false}){Text("Cancel")}})
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("New disciplinary action",back=onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).imePadding().padding(20.dp)) {
            FormSection("01","Member and action","Only members returned by your authorized directory are selectable.")
            KpnDropdown("Member",member,s.members,{"${it.name} · ${it.role_title ?: it.role}"},{member=it})
            KpnDropdown("Action",type,listOf("WARNING","SUSPENSION","DISMISSAL"),{it.lowercase().replaceFirstChar{it.uppercase()}},{type=it})
            KpnField("Reason",reason,{reason=it},lines=5,error=s.errors["discipline"]?.fields?.get("reason"))
            ErrorState(s.errors["members"]){vm.loadMembers(false)};ErrorState(s.errors["discipline"]){vm.clearError("discipline")}
            KpnPrimaryButton("Review and submit",s.busy,member!=null&&reason.isNotBlank()){confirm=true}
        }
    }
}

@Composable internal fun MemberAdministrationScreen(vm:AppViewModel,s:AppState,nav:NavHostController,id:Int) {
    var action by remember(id){mutableStateOf("edit_role")};var values by remember(id,action){mutableStateOf<Map<String,String>>(emptyMap())};var confirming by remember{mutableStateOf(false)}
    LaunchedEffect(id,action){vm.loadMemberAdministration(id,action)}
    val form=s.memberAdministration?.takeIf{it.member.id==id&&it.action==action}
    LaunchedEffect(form?.action,form?.member?.id){if(form!=null&&values.isEmpty())values=form.fields.associate{it.name to it.default_value.orEmpty()}}
    if(confirming&&form!=null)AlertDialog(onDismissRequest={confirming=false},title={Text("Confirm ${form.title.lowercase()}?")},text={Text("Apply this change to ${form.member.name}? It affects the same membership account used by the website. Dismissal deactivates login; website reinstatement alone does not reactivate a dismissed account.")},confirmButton={TextButton(enabled=!s.busy,onClick={vm.administerMember(id,action,values){confirming=false;nav.popBackStack()}}){Text("Confirm change",color=MaterialTheme.colorScheme.error)}},dismissButton={TextButton(onClick={confirming=false}){Text("Cancel")}})
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Membership administration",back={nav.popBackStack()})
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().padding(20.dp)) {
            ErrorState(s.errors["member_admin"]){vm.clearError("member_admin")}
            form?.let{f->Text(f.member.name,style=MaterialTheme.typography.titleLarge);StatusBadge(f.member.status);FormSection("01","President-only action");KpnDropdown("Action",f.actions.firstOrNull{it.value==action},f.actions,{it.label},{action=it.value})
                f.fields.forEach{field->KpnDropdown(field.label,field.choices.firstOrNull{it.value==values[field.name]},field.choices,{it.label},{choice->
                    values=values+(field.name to choice.value)
                    if(field.name=="zone"){values=values+("lga" to "")+("ward" to "");vm.loadMemberAdministration(id,action,values)}
                    if(field.name=="lga"){values=values+("ward" to "");vm.loadMemberAdministration(id,action,values)}
                    if(field.name=="new_role"){values=values+("new_role_definition" to "");vm.loadMemberAdministration(id,action,values)}
                });s.errors["member_admin"]?.fields?.get(field.name)?.let{Text(it,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)}}
                if(action in listOf("suspend","dismiss"))KpnField("Reason",values["reason"].orEmpty(),{values=values+("reason" to it)},lines=4)
                val complete=f.fields.filter{it.required}.all{!values[it.name].isNullOrBlank()}&&(action !in listOf("suspend","dismiss")||!values["reason"].isNullOrBlank())
                KpnPrimaryButton("Review change",s.busy,complete){confirming=true}
            } ?: Loading(true)
            Spacer(Modifier.height(24.dp))
        }
    }
}
