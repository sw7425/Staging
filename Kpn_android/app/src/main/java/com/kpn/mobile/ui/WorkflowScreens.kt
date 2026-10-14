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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kpn.mobile.core.*
import com.kpn.mobile.data.*

// ── F2 · Member Approvals / Members Screen ────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MembersScreen(vm: AppViewModel, s: AppState, nav: NavHostController, policy: RolePolicy) {
    val canApprove = "REVIEW_APPLICANTS" in s.user?.capabilities.orEmpty()
    var showPending by remember { mutableStateOf(canApprove) }
    var viewing by remember { mutableStateOf<Member?>(null) }
    var search by remember { mutableStateOf("") };var gender by remember { mutableStateOf(if(s.user?.role_title in setOf("Director of Women's Development","Assistant Director of Women's Development","LGA Women's Development Officer"))"F" else "All") };var filterStatus by remember { mutableStateOf("All") }
    var exportMenu by remember { mutableStateOf(false) };var showFilters by remember { mutableStateOf(false) };var decisionMessage by remember { mutableStateOf<String?>(null) }
    val context=androidx.compose.ui.platform.LocalContext.current
    val csvPicker=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")){uri->uri?.let{vm.exportMembers("csv",it){android.widget.Toast.makeText(context,"Member CSV saved",android.widget.Toast.LENGTH_SHORT).show()}}}
    val pdfPicker=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")){uri->uri?.let{vm.exportMembers("pdf",it){android.widget.Toast.makeText(context,"Member PDF saved",android.widget.Toast.LENGTH_SHORT).show()}}}
    val visible=s.members.filter{m->(gender=="All"||m.gender==gender)&&(filterStatus=="All"||m.status==filterStatus)&&(search.isBlank()||("${m.name} ${m.username} ${m.role_title ?: m.role} ${m.lga_name} ${m.ward_name}").contains(search,ignoreCase=true))}
    var decision by remember { mutableStateOf<Pair<Member,String>?>(null) }
    LaunchedEffect(showPending) { vm.loadMembers(showPending) }
    viewing?.let { initial ->
        val member=s.selectedMember?.takeIf{it.id==initial.id} ?: initial
        ModalBottomSheet(sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),onDismissRequest={viewing=null}) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically){KpnAvatar(member.photo,member.name,64);Spacer(Modifier.width(16.dp));Column{Text(member.name,style=MaterialTheme.typography.titleLarge);StatusBadge(if(member.archived_at!=null)"ARCHIVED" else member.status)}}
                if(member.archived_at!=null)InfoBanner("Archived application. Sign-in is disabled; records are preserved.")
                SectionHeading("Membership profile")
                listOf("Position" to (member.role_title ?: member.role),"Zone" to member.zone_name,"LGA" to member.lga_name,"Ward" to member.ward_name,"Email" to member.email,"Phone" to member.phone,"Gender" to member.gender,"Application date" to member.created_at).forEach{(label,value)->if(!value.isNullOrBlank()){Text(label,style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(value,style=MaterialTheme.typography.bodyMedium,modifier=Modifier.padding(bottom=12.dp))}}
                ErrorState(s.errors["member_detail"]){vm.loadMember(member.id)}
                if(member.archived_at==null&&"STAFF_ADMIN" in s.user?.capabilities.orEmpty())ActionRow("Administer membership","Authorized role changes, jurisdiction, suspension and reinstatement"){viewing=null;nav.navigate("member_admin/${member.id}")}
                if(canApprove&&member.status in listOf("PENDING","UNDER_REVIEW")) {
                    InfoBanner("Review the profile, jurisdiction and requested position before deciding.")
                    Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){OutlinedButton(shape=MaterialTheme.shapes.small, modifier=(Modifier.weight(1f)).heightIn(min=48.dp),onClick={vm.clearError("member_decision");decision=member to "reject"}){Text("Reject",color=MaterialTheme.colorScheme.error)};Button(shape=MaterialTheme.shapes.small, modifier=(Modifier.weight(1f)).heightIn(min=48.dp),onClick={vm.clearError("member_decision");decision=member to "approve"}){Text("Approve")}}
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Members",back={nav.popBackStack()},actions={if("EXPORT_MEMBERS" in s.user?.capabilities.orEmpty())Box{IconButton(onClick={exportMenu=true}){Icon(Icons.Default.Download,"Export verified members")};DropdownMenu(expanded=exportMenu,onDismissRequest={exportMenu=false}){DropdownMenuItem(text={Text("Export CSV")},onClick={exportMenu=false;csvPicker.launch("kpn_verified_members.csv")});DropdownMenuItem(text={Text("Export PDF")},onClick={exportMenu=false;pdfPicker.launch("kpn_verified_members.pdf")})}}})
        if(canApprove)TabRow(selectedTabIndex=if(showPending)0 else 1){Tab(selected=showPending,onClick={showPending=true},text={Text("Applicants")});Tab(selected=!showPending,onClick={showPending=false},text={Text("Directory")})}
        Text(s.user?.ward_name ?: s.user?.lga_name ?: s.user?.zone_name ?: "Kebbi State",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(horizontal=20.dp,vertical=12.dp))
        KpnField("Search members or location",search,{search=it},modifier=Modifier.padding(horizontal=20.dp))
        TextButton(onClick={showFilters=!showFilters},modifier=Modifier.padding(horizontal=12.dp)){Icon(Icons.Default.Tune,null);Spacer(Modifier.width(8.dp));Text(if(showFilters)"Hide filters" else "Filters · ${if(gender=="All")"All genders" else gender} · ${filterStatus.replace('_',' ')}")}
        if(showFilters) Column(Modifier.padding(horizontal=20.dp)) {
            KpnDropdown("Gender",gender,listOf("All","F","M"),{when(it){"F"->"Female";"M"->"Male";else->"All"}},{gender=it})
            KpnDropdown("Status",filterStatus,listOf("All","VERIFIED","PENDING","UNDER_REVIEW","SUSPENDED","REJECTED"),{it.replace('_',' ').lowercase().replaceFirstChar{c->c.uppercase()}},{filterStatus=it})
        }
        decisionMessage?.let { message -> Column(Modifier.padding(horizontal=20.dp,vertical=8.dp)) { InfoBanner(message,tone=BannerTone.SUCCESS) } }

        Loading(s.busy);ErrorState(s.errors["members"]){vm.loadMembers(showPending)};ErrorState(s.errors["member_export"]){vm.clearError("member_export")}
        LazyColumn(contentPadding=PaddingValues(horizontal=20.dp)) {
            if(visible.isEmpty()&&!s.busy&&s.errors["members"]==null)item{EmptyState(if(showPending)"No pending applicants" else "No members found")}
            items(visible,key={it.id}) { member ->
                DashRow(member.name.take(1),member.name,"${member.role_title ?: member.role} · ${member.ward_name ?: member.lga_name ?: member.zone_name ?: "KPN"}",if(member.archived_at!=null)"ARCHIVED" else member.status,onClick={viewing=member;vm.loadMember(member.id)})
                KpnDivider()
            }
        }
    }
    decision?.let { (member, action) ->
        MemberDecisionDialog(member,action,"member_decision" in s.loading,s.errors["member_decision"],
            onConfirm={vm.decideMember(member.id,action,showPending) {
                decision=null;viewing=null
                decisionMessage=if(action=="approve")"Membership approved. Member is verified." else "Application rejected and archived. Records preserved."
            }},onCancel={decision=null;vm.clearError("member_decision")})
    }

}

// ── J1 · Hierarchical Report Submission ───────────────────────────────────────

@Composable
internal fun ReportSubmitScreen(vm: AppViewModel, s: AppState, nav: NavHostController) {
    var title    by remember { mutableStateOf("") }
    var period   by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var content  by remember { mutableStateOf("") }

    val user = s.user
    val (submittedTo, routeNote) = when (user?.tier) {
        "WARD"    -> Pair("LGA Network Lead", "Ward → LGA")
        "LGA"     -> Pair("Senatorial Director", "LGA → Senatorial")
        "ZONAL"   -> Pair("State", "Senatorial → State")
        else      -> Pair("KPN State", "Submitted to state")
    }

    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Submit report", back = { nav.popBackStack() })

        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            // Hierarchy note banner
            InfoBanner(
                "Recipient is selected from the hierarchy. $submittedTo [$routeNote] for the Ward/LGA.",
                isWarning = true,
            )
            Spacer(Modifier.height(12.dp))

            Text("TITLE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            KpnField("Report title", title, { title = it })

            Text("REPORTING PERIOD", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            KpnField("Month, quarter or week", period, { period = it })

            Text("DEADLINE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            KpnDateField("Optional report deadline",deadline,{deadline=it},error=s.errors["submit_report"]?.fields?.get("deadline"))

            Text("REPORT CONTENT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            KpnField("Detailed operational report", content, { content = it }, lines = 7)

            Spacer(Modifier.height(8.dp))
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                Text(
                    "Submitted to: $submittedTo [$routeNote · jurisdiction derived]",
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                )
            }

            Spacer(Modifier.height(12.dp))
            ErrorState(s.errors["submit_report"]) { vm.clearError("submit_report") }
            Loading(s.busy)

            Button(shape=MaterialTheme.shapes.small, 
                onClick = {
                    vm.submitReport(title, content, period, deadline.ifBlank { null }) {
                        if (it) nav.popBackStack()
                    }
                },
                enabled  = !s.busy && title.isNotBlank() && content.isNotBlank(),
                modifier =(Modifier.fillMaxWidth()).heightIn(min=48.dp),
            ) { Text(if (s.busy) "Submitting…" else "Submit report") }
        }
    }
}

// ── Reports list + J2 · Report Review ─────────────────────────────────────────

@Composable
internal fun ReportsScreen(vm: AppViewModel, s: AppState, nav: NavHostController, policy: RolePolicy) {
    LaunchedEffect(Unit) { vm.loadReports() }
    val canReview = Capability.REVIEW_REPORTS in policy.capabilities
    val canSubmit = Capability.REPORTS in policy.capabilities && policy.tier != Tier.STATE

    Column(Modifier.fillMaxSize()) {
        KpnTopBar(
            title   = "Reports",
            back    = { nav.popBackStack() },
            actions = {
                if (canSubmit) {
                    IconButton(onClick = { nav.navigate("submit_report") }) {
                        Icon(Icons.Default.Add, "Submit report")
                    }
                }
            },
        )
        Loading(s.busy)
        ErrorState(s.errors["reports"]) { vm.clearError("reports") }

        LazyColumn(Modifier.padding(horizontal = 16.dp)) {
            if (s.reports.isEmpty() && !s.busy) {
                item { EmptyState("No reports in this queue") }
            }
            items(s.reports) { r ->
                Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(r.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            StatusBadge(r.status)
                        }
                        Text(
                            listOfNotNull(r.period, r.submitted_by_name, r.deadline?.let { "Due: ${KpnDates.date(it)}" }).joinToString(" · "),
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (r.review_notes.isNotBlank()) {
                            Text(r.review_notes, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                        }
                        if (canReview && r.status in listOf("SUBMITTED", "UNDER_REVIEW")) {
                            ReportReviewActions(r, vm, nav)
                        }
                        if (r.status == "APPROVED" && !r.is_escalated && canReview) {
                            TextButton(onClick = { vm.escalateReport(r.id) }) { Text("Escalate to next level") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportReviewActions(r: ReportItem, vm: AppViewModel, nav: NavHostController) {
    var reviewNotes by remember { mutableStateOf("") }
    var showNotes   by remember { mutableStateOf(false) }

    Spacer(Modifier.height(8.dp))
    if (showNotes) {
        KpnField("Review notes", reviewNotes, { reviewNotes = it }, lines = 3)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = { showNotes = !showNotes }) { Text(if (showNotes) "Hide notes" else "Add notes") }
        Spacer(Modifier.weight(1f))
        OutlinedButton(shape=MaterialTheme.shapes.small, modifier=Modifier.heightIn(min=48.dp), 
            onClick = { vm.reviewReport(r.id, "flag",   reviewNotes) },
            colors  = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.tertiary),
        ) { Text("Flag") }
        OutlinedButton(shape=MaterialTheme.shapes.small, modifier=Modifier.heightIn(min=48.dp), 
            onClick = { vm.reviewReport(r.id, "reject", reviewNotes) },
            colors  = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) { Text("Reject") }
        Button(shape=MaterialTheme.shapes.small, modifier=Modifier.heightIn(min=48.dp), onClick = { vm.reviewReport(r.id, "approve", reviewNotes) }) { Text("Approve & escalate") }
    }
}

// ── J3 · Editorial Review Queue ───────────────────────────────────────────────

@Composable
internal fun EditorialScreen(vm: AppViewModel, s: AppState, nav: NavHostController) {
    LaunchedEffect(Unit) { vm.loadEditorialQueue() }
    var returning by remember { mutableStateOf<Article?>(null) }; var note by remember { mutableStateOf("") }
    returning?.let { a -> AlertDialog(onDismissRequest={returning=null}, title={Text("Return article to author")}, text={Column {Text(a.title); KpnField("Required editorial note",note,{note=it},lines=4); ErrorState(s.errors["editorial"]){vm.clearError("editorial")}}}, confirmButton={TextButton(onClick={vm.decideArticle(a.id,"return",note){if(it)returning=null}},enabled=note.isNotBlank()&&!s.busy){Text("Return article")}},dismissButton={TextButton(onClick={returning=null}){Text("Cancel")}}) }
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Editorial review",back={nav.popBackStack()}); Loading(s.busy); ErrorState(s.errors["editorial"]){vm.loadEditorialQueue()}
        LazyColumn(contentPadding=PaddingValues(20.dp)) {
            item {SectionHeading("Pending review","Articles submitted through the KPN editorial workflow")}
            if(s.editorialQueue.isEmpty()&&!s.busy&&s.errors["editorial"]==null)item{EmptyState("No articles awaiting review.")}
            items(s.editorialQueue,key={it.id}) {a -> Column(Modifier.padding(vertical=12.dp)) {
                Text(a.title,style=MaterialTheme.typography.titleMedium); a.status?.let{StatusBadge(it)}
                Spacer(Modifier.height(12.dp)); ArticleBody(a)
                Row {OutlinedButton(shape=MaterialTheme.shapes.small, onClick={note="";returning=a},modifier=(Modifier.weight(1f)).heightIn(min=48.dp)){Text("Return")};Spacer(Modifier.width(12.dp)); Button(shape=MaterialTheme.shapes.small, onClick={vm.decideArticle(a.id,"publish")},enabled=!s.busy,modifier=(Modifier.weight(1f)).heightIn(min=48.dp)){Text("Publish")}}
                Spacer(Modifier.height(16.dp));KpnDivider()
            }}
        }
    }
}

// ── J4 · Article Composer ─────────────────────────────────────────────────────

@Composable
internal fun ArticleComposerScreen(vm:AppViewModel,s:AppState,nav:NavHostController,editingId:Int?=null)=NativeArticleEditorScreen(vm,s,nav,editingId)

// ── J5 · Event Attendance ─────────────────────────────────────────────────────

@Composable
internal fun EventAttendanceScreen(vm:AppViewModel,s:AppState,nav:NavHostController) {
    LaunchedEffect(Unit){vm.loadEvents();vm.loadMembers(false)}
    val event=s.selectedEvent
    LaunchedEffect(event?.id){event?.let{vm.loadAttendance(it.id)}}
    var changed by remember(event?.id){mutableStateOf<Map<Int,Boolean>>(emptyMap())}
    val existing=s.attendance.associate{it.attendee to it.present}
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Event attendance",back={nav.popBackStack()})
        KpnDropdown("Select event",event,s.events,{it.title},{vm.selectEvent(it)},modifier=Modifier.padding(horizontal=20.dp))
        Loading(s.busy);ErrorState(s.errors["attendance"]){event?.let{vm.loadAttendance(it.id)}}
        LazyColumn(contentPadding=PaddingValues(20.dp)) {
            if(event==null)item{EmptyState("Select the event whose attendance you want to record.")}
            if(event!=null){item{SectionHeading(event.title,event.location)}
                items(s.members,key={it.id}) {m-> Row(Modifier.fillMaxWidth().padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(m.name,style=MaterialTheme.typography.titleSmall);Text(m.ward_name ?: m.lga_name.orEmpty(),style=MaterialTheme.typography.bodySmall)};Switch(checked=changed[m.id] ?: existing[m.id] ?: false,onCheckedChange={changed=changed+(m.id to it)})};KpnDivider()}
                item{KpnPrimaryButton("Save attendance",s.busy,changed.isNotEmpty()&&s.errors["attendance"]==null){vm.saveAttendance(event.id,changed.map{(id,present)->AttendanceRecord(attendee=id,present=present)}){if(it)nav.popBackStack()}}}
            }
        }
    }
}

// ── Events screen ──────────────────────────────────────────────────────────────

@Composable
internal fun EventsScreen(vm: AppViewModel, s: AppState, nav: NavHostController, canCreate: Boolean = false) {
    LaunchedEffect(Unit) { vm.loadEvents() }

    Column(Modifier.fillMaxSize()) {
        KpnTopBar(
            title   = "Events & meetings",
            back    = { nav.popBackStack() },
            actions = {
                if (canCreate) {
                    IconButton(onClick = { nav.navigate("create_event") }) { Icon(Icons.Default.Add, "Create event") }
                }
            },
        )
        Loading(s.busy)
        LazyColumn(Modifier.padding(horizontal = 16.dp)) {
            if (s.events.isEmpty() && !s.busy) {
                item { EmptyState("No events or meetings") }
            }
            items(s.events) { ev ->
                DashRow(
                    letter   = if (ev.description.contains("meeting", true)) "M" else "E",
                    title    = ev.title,
                    subtitle = "${KpnDates.dateTime(ev.start_date)} · ${ev.location}",
                    onClick  = { vm.selectEvent(ev); nav.navigate("event/${ev.id}") },
                )
                KpnDivider()
            }
        }
    }
}

// ── J6 · Programme Management ─────────────────────────────────────────────────

@Composable
internal fun ProgramsScreen(vm: AppViewModel, s: AppState, nav: NavHostController, kind: String) {
    LaunchedEffect(kind) { vm.loadPrograms(kind) }
    var participantProgram by remember { mutableStateOf<ProgramItem?>(null) }
    participantProgram?.let { p -> ParticipantEditor(vm,s,kind,p.id,p.title){participantProgram=null} }

    val tabs   = listOf("All", "Planned", "Ongoing", "Completed", "Cancelled")
    var tabIdx by remember { mutableIntStateOf(0) }
    val status = listOf("", "PLANNED", "ONGOING", "COMPLETED", "CANCELLED")[tabIdx]
    val displayed = s.programs.filter { status.isBlank()||it.status.equals(status, ignoreCase = true) }
    val wardUnsupported=s.user?.tier=="WARD"

    Column(Modifier.fillMaxSize()) {
        KpnTopBar(
            title = "${kind.replaceFirstChar { it.uppercase() }} programmes",
            back  = { nav.popBackStack() },actions={if(!wardUnsupported)IconButton(onClick={nav.navigate("resource/programs_$kind")}){Icon(Icons.Default.Edit,"Edit programmes")}},
        )
        Loading(s.busy);ErrorState(s.errors["programs"]){vm.loadPrograms(kind)}
        if(wardUnsupported)InfoBanner("Ward-specific programme management is not configured in the website data model. LGA-wide and State-wide access will not be granted.")
        ScrollableTabRow(selectedTabIndex = tabIdx,edgePadding=16.dp) {
            tabs.forEachIndexed { i, t -> Tab(selected = tabIdx == i, onClick = { tabIdx = i }, text = { Text(t) }) }
        }
        LazyColumn(Modifier.padding(horizontal = 16.dp)) {
            if (displayed.isEmpty() && !s.busy) {
                item { EmptyState("No ${tabs[tabIdx].lowercase()} programmes") }
            }
            items(displayed) { p ->
                Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(p.status)
                            p.location?.let {
                                Spacer(Modifier.width(8.dp))
                                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(p.title, style = MaterialTheme.typography.titleMedium)
                        if (p.description.isNotBlank()) {
                            Text(p.description, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                        }
                        if("MANAGE_PROGRAM_PARTICIPANTS" in s.user?.capabilities.orEmpty()&&!wardUnsupported)TextButton(onClick = { participantProgram = p; vm.loadMembers(false); vm.loadProgramParticipants(kind,p.id) }) { Text("Manage participants") }
                    }
                }
            }
            if(!wardUnsupported)item {
                Button(shape=MaterialTheme.shapes.small, 
                    onClick  = { nav.navigate("create_program/$kind") },
                    modifier =(Modifier.fillMaxWidth().padding(vertical = 8.dp)).heightIn(min=48.dp),
                ) { Text("Create program") }
            }
        }
    }
}

// ── Finance Screen ─────────────────────────────────────────────────────────────

@Composable
internal fun FinanceScreen(vm: AppViewModel, s: AppState, nav: NavHostController) {
    LaunchedEffect(Unit) { vm.loadFinance(); vm.loadDonations() }

    Column(Modifier.fillMaxSize()) {
        KpnTopBar("State finance", back = { nav.popBackStack() })
        ErrorState(s.errors["verifyDonation"] ?: s.errors["recordDonation"]){vm.clearError("verifyDonation");vm.clearError("recordDonation")}
        Loading(s.busy)
        ErrorState(s.errors["finance"]) { vm.clearError("finance") }

        val f = s.finance
        if (f == null && !s.busy) {
            CenteredMessage("Finance data is unavailable for this role or scope.\n\nFinance is only accessible to authorised State-level officers.")
        } else f?.let { fin ->
            LazyColumn(Modifier.padding(horizontal = 16.dp)) {
                item {
                    SectionHeading("Finance","State-level records from the KPN backend")
                    val caps=s.user?.capabilities.orEmpty()
                    if("RECORD_DONATION" in caps)ActionRow("Add donation","Enter a donation for verification"){nav.navigate("resource/donations")}
                    if("RECORD_EXPENSE" in caps)ActionRow("Expense records","View and record authorized expenditure"){nav.navigate("resource/expenses")}
                    Spacer(Modifier.height(12.dp))
                }
                item { LiveMetricPair("₦${fin.total_income}", "TOTAL INCOME", "₦${fin.total_expenses}", "TOTAL EXPENSES") }
                item { LiveMetricCard("₦${fin.balance}", "BALANCE") }
                item {
                    Spacer(Modifier.height(8.dp))
                    DashRow("D", "Pending donations", "${fin.pending_donations} unverified")
                    KpnDivider()
                }
                if (s.donations.isNotEmpty()) {
                    item {
                        Text("Recent donations", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(s.donations,key={it.id}) { d ->
                        DashRow("D", d.donor_name, "₦${d.amount} · ${d.reference}", status = d.status)
                        if(d.status=="UNVERIFIED"&&"VERIFY_DONATION" in s.user?.capabilities.orEmpty())TextButton(onClick={vm.verifyDonation(d.id)},enabled=!s.busy){Text("Verify donation")}
                        if(d.status=="VERIFIED"&&"RECORD_EXPENSE" in s.user?.capabilities.orEmpty())TextButton(onClick={vm.recordDonation(d.id)},enabled=!s.busy){Text("Record verified donation")}
                        KpnDivider()
                    }
                }
            }
        }
    }
}

// ── Discipline / Legal Review Screen ──────────────────────────────────────────

@Composable
internal fun DisciplineScreen(vm: AppViewModel, s: AppState, nav: NavHostController) {
    LaunchedEffect(Unit) { vm.loadDiscipline() }
    
    var showOpinionFor by remember { mutableStateOf<Discipline?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deciding by remember { mutableStateOf<Pair<Discipline,String>?>(null) }
    if(creating) { DisciplineCreateScreen(vm,s,onBack={creating=false});return }
    deciding?.let { (action,decision) ->
        AlertDialog(onDismissRequest={deciding=null},title={Text(if(decision=="approve")"Approve disciplinary action?" else "Reject disciplinary action?")},text={Text(if(decision=="approve")"${action.action_type.lowercase().replaceFirstChar{it.uppercase()}} for member ${action.user}. Suspension or dismissal changes account access. Dismissal requires a favorable legal opinion." else "Reject and remove this pending action? This follows the website's irreversible rejection workflow.")},confirmButton={TextButton(enabled=!s.busy,onClick={vm.decideDisciplineAction(action.id,decision){deciding=null}}){Text("Confirm",color=MaterialTheme.colorScheme.error)}},dismissButton={TextButton(onClick={deciding=null}){Text("Cancel")}})
    }
    var opinionText by remember { mutableStateOf("") }; var legalFavorable by remember{mutableStateOf(false)}
    
    showOpinionFor?.let { d ->
        AlertDialog(
            onDismissRequest = { showOpinionFor = null },
            title = { Text("Legal assessment") },
            text = {
                Column {
                    Text("Provide your legal and ethical assessment for this action.", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    KpnField("Opinion text", opinionText, { opinionText = it }, lines = 4)
                    Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=legalFavorable,onCheckedChange={legalFavorable=it});Text("Favorable legal opinion")}
                }
            },
            confirmButton = {
                Button(shape=MaterialTheme.shapes.small, modifier=Modifier.heightIn(min=48.dp), onClick = {
                    vm.submitLegalOpinion(d.id, LegalOpinionRequest(opinionText, legal_approved = legalFavorable)) {
                        showOpinionFor = null
                    }
                }, enabled = opinionText.isNotBlank() && !s.busy) { Text("Submit legal opinion") }
            },
            dismissButton = {
                OutlinedButton(shape=MaterialTheme.shapes.small, modifier=Modifier.heightIn(min=48.dp), onClick = { showOpinionFor = null }) { Text("Cancel") }
            }
        )
    }

    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Legal & discipline", back = { nav.popBackStack() }, actions={if("CREATE_DISCIPLINE" in s.user?.capabilities.orEmpty())IconButton(onClick={creating=true}){Icon(Icons.Default.Add,"Create disciplinary action")}})
        Loading(s.busy)
        ErrorState(s.errors["discipline"] ?: s.errors["legalOpinion"]) { 
            vm.clearError("discipline"); vm.clearError("legalOpinion")
        }

        LazyColumn(Modifier.padding(horizontal = 16.dp)) {
            if (s.disciplines.isEmpty() && !s.busy) {
                item { EmptyState("No disciplinary actions") }
            }
            items(s.disciplines) { d ->
                Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(d.action_type.replace('_', ' '), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            StatusBadge(if (d.is_approved) "APPROVED" else "PENDING")
                        }
                        Text("Member ID: ${d.user}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(d.reason, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                        if (d.legal_opinion.isNotBlank()) {
                            Text("Legal opinion: ${d.legal_opinion}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                        }
                        if (!d.is_approved && "LEGAL_REVIEW" in s.user?.capabilities.orEmpty()) TextButton(onClick={opinionText=d.legal_opinion;showOpinionFor=d}){Text("Provide legal opinion")}
                        if (!d.is_approved && "DECIDE_DISCIPLINE" in s.user?.capabilities.orEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                                OutlinedButton(shape=MaterialTheme.shapes.small, modifier=Modifier.heightIn(min=48.dp), 
                                    onClick = { deciding=d to "reject" },
                                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                ) { Text("Reject") }
                                Button(shape=MaterialTheme.shapes.small, modifier=Modifier.heightIn(min=48.dp), onClick = { 
                                    deciding=d to "approve"
                                }) { Text("Approve") }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Media Screen ───────────────────────────────────────────────────────────────

@Composable
internal fun MediaScreen(vm: AppViewModel, s: AppState, nav: NavHostController, review: Boolean) {
    LaunchedEffect(review) { vm.loadMedia(review) }

    Column(Modifier.fillMaxSize()) {
        KpnTopBar(if (review) "Media review" else "My media", back = { nav.popBackStack() },actions={if("UPLOAD_MEDIA" in s.user?.capabilities.orEmpty())IconButton(onClick={nav.navigate("media_upload")}){Icon(Icons.Default.Add,"Upload media")}})
        Loading(s.busy)
        LazyColumn(Modifier.padding(horizontal = 16.dp)) {
            if (s.media.isEmpty() && !s.busy) item { EmptyState("No media items") }
            items(s.media) { m ->
                DashRow(
                    letter   = m.media_type.take(1),
                    title    = m.title,
                    subtitle = m.media_type,
                    status   = m.status,
                )
                if (review && m.status == "PENDING") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Spacer(Modifier.weight(1f))
                        OutlinedButton(shape=MaterialTheme.shapes.small, modifier=Modifier.heightIn(min=48.dp), onClick = { vm.decideMedia(m.id, "reject") }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Reject") }
                        Button(shape=MaterialTheme.shapes.small, modifier=Modifier.heightIn(min=48.dp), onClick = { vm.decideMedia(m.id, "approve") }) { Text("Approve") }
                    }
                }
                KpnDivider()
            }
        }
    }
}

// ── G2 · Ward Meeting Form ────────────────────────────────────────────────────

@Composable
internal fun WardMeetingFormScreen(vm: AppViewModel, s: AppState, nav: NavHostController) {
    var title    by remember { mutableStateOf("") }
    var date     by remember { mutableStateOf("") }
    var time     by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var agenda   by remember { mutableStateOf("") }

    val user     = s.user
    val wardName = user?.ward_name ?: "Ward"

    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Ward meeting", back = { nav.popBackStack() })

        Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
            Text("Create record", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
        }

        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("TITLE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            KpnField("Meeting title", title, { title = it })

            Text("DATE & TIME", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) { KpnField("Date (YYYY-MM-DD)", date, { date = it }) }
                Box(Modifier.weight(1f)) { KpnField("Time (HH:MM)", time, { time = it }) }
            }

            Text("LOCATION", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            KpnField("Meeting venue", location, { location = it })

            Text("AGENDA", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            KpnField("Agenda and topics", agenda, { agenda = it }, lines = 5)

            Spacer(Modifier.height(8.dp))
            ErrorState(s.errors["ward_meeting"]) { vm.clearError("ward_meeting") }
            Loading(s.busy)

            Button(shape=MaterialTheme.shapes.small, 
                onClick = {
                    vm.createWardMeeting(
                        title    = title,
                        date     = date,
                        time     = time.ifBlank { null },
                        location = location,
                        agenda   = agenda,
                    ) { if (it) nav.popBackStack() }
                },
                enabled  = !s.busy && title.isNotBlank() && date.isNotBlank() && location.isNotBlank(),
                modifier =(Modifier.fillMaxWidth()).heightIn(min=48.dp),
            ) { Text("Save meeting") }

            Spacer(Modifier.height(8.dp))
            Text(
                "Quorum: $wardName. Only Ward Community Lead (and Ward Administrative Officer) may create Ward Meeting records.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Ward Meetings list ─────────────────────────────────────────────────────────

@Composable
internal fun WardMeetingsScreen(vm: AppViewModel, s: AppState, nav: NavHostController) {
    LaunchedEffect(Unit) { vm.loadWardMeetings() }
    var attendanceMeeting by remember{mutableStateOf<WardMeeting?>(null)}
    attendanceMeeting?.let{m->WardAttendanceEditor(vm,s,m){attendanceMeeting=null}}

    Column(Modifier.fillMaxSize()) {
        KpnTopBar(
            title   = "Ward meetings",
            back    = { nav.popBackStack() },
            actions = {
                if("MANAGE_WARD_MEETINGS" in s.user?.capabilities.orEmpty()) IconButton(onClick = { nav.navigate("ward_meeting_form") }) { Icon(Icons.Default.Add, "Create") }
            },
        )
        Loading(s.busy)
        LazyColumn(Modifier.padding(horizontal = 16.dp)) {
            if (s.wardMeetings.isEmpty() && !s.busy) item { EmptyState("No ward meeting records") }
            items(s.wardMeetings) { m ->
                DashRow("M", m.title, "${m.date}${m.time?.let { " · $it" } ?: ""} · ${m.location}")
                if(m.agenda.isNotBlank()){SectionHeading("Agenda");ReadableBody(m.agenda)}
                if(m.minutes.isNotBlank()){SectionHeading("Minutes");ReadableBody(m.minutes)}
                if("MANAGE_WARD_MEETINGS" in s.user?.capabilities.orEmpty())TextButton(onClick={nav.navigate("resource/ward-meetings")}){Text("Edit meeting / minutes")}
                if("MANAGE_WARD_ATTENDANCE" in s.user?.capabilities.orEmpty())TextButton(onClick={attendanceMeeting=m}){Text("Record attendance")}
                KpnDivider()
            }
        }
    }
}

// ── J8 · Community Report (Trusted Reporter form) ─────────────────────────────

@Composable
internal fun CommunityReportScreen(vm: AppViewModel, s: AppState, nav: NavHostController) {
    val user = s.user ?: return

    var locationDetails by remember { mutableStateOf("") }
    var incidentDate    by remember { mutableStateOf("") }
    var incidentTime    by remember { mutableStateOf("") }
    var category        by remember { mutableStateOf("COMMUNITY") }
    var whatHappened    by remember { mutableStateOf("") }
    var whoWasInvolved  by remember { mutableStateOf("") }
    var whyImportant    by remember { mutableStateOf("") }
    var image by remember { mutableStateOf<Uri?>(null) };var video by remember { mutableStateOf<Uri?>(null) }
    val imagePicker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){image=it}
    val videoPicker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){video=it}

    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Community report", back = { nav.popBackStack() })

        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("REPORTER", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                Text("Prefilled from profile: ${user.displayName}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
            }

            Text("INCIDENT DETAILS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            KpnField("Specific location / Landmark", locationDetails, { locationDetails = it })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpnField("Date (YYYY-MM-DD)", incidentDate, { incidentDate = it }, modifier = Modifier.weight(1f))
                KpnField("Time (Optional)", incidentTime, { incidentTime = it }, modifier = Modifier.weight(1f))
            }

            Text("CATEGORY", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            KpnDropdown("Category",category,ContentChoices.communityReports.map{it.first},{key->ContentChoices.communityReports.first{it.first==key}.second},{category=it})

            Text("REPORT CONTENT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            KpnField("What happened?", whatHappened, { whatHappened = it }, lines = 4)
            KpnField("Who was involved?", whoWasInvolved, { whoWasInvolved = it }, lines = 2)
            KpnField("Why is this important?", whyImportant, { whyImportant = it }, lines = 2)

            Spacer(Modifier.height(8.dp))
            SectionHeading("Evidence",supporting="Optional supporting photo or video. Select files smaller than 20 MB.")
            if(image!=null)KpnImage(image.toString(),"Selected evidence photo",Modifier.fillMaxWidth().height(160.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(shape=MaterialTheme.shapes.small, onClick={imagePicker.launch("image/*")},modifier=(Modifier.weight(1f)).heightIn(min=48.dp)){Text(if(image==null)"Add photo" else "Change photo")};OutlinedButton(shape=MaterialTheme.shapes.small, onClick={videoPicker.launch("video/*")},modifier=(Modifier.weight(1f)).heightIn(min=48.dp)){Text(if(video==null)"Add video" else "Change video")}}
            if(image!=null||video!=null)TextButton(onClick={image=null;video=null}){Text("Remove evidence")}
            if(video!=null)Text("Video selected",style=MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))

            ErrorState(s.errors["submitCommunityReport"]) { vm.clearError("submitCommunityReport") }
            Loading(s.busy)

            Button(shape=MaterialTheme.shapes.small, 
                onClick = {
                    val cr = com.kpn.mobile.data.CommunityReport(
                        location_details = locationDetails,
                        incident_date = incidentDate,
                        incident_time = incidentTime,
                        category = category,
                        what_happened = whatHappened,
                        who_was_involved = whoWasInvolved,
                        why_is_it_important = whyImportant
                    )
                    vm.submitCommunityReport(cr,image,video) {
                        nav.popBackStack()
                    }
                },
                enabled  = !s.busy && locationDetails.isNotBlank() && whatHappened.isNotBlank() && incidentDate.isNotBlank(),
                modifier =(Modifier.fillMaxWidth()).heightIn(min=48.dp),
            ) { Text("Submit for review") }
        }
    }
}

// ── E2 · Senatorial Report Review ─────────────────────────────────────────────

@Composable
internal fun SenatorReportReviewScreen(vm: AppViewModel, s: AppState, nav: NavHostController, policy: RolePolicy) {
    LaunchedEffect(Unit) { vm.loadReports() }

    val tabs   = listOf("Submitted", "Flagged", "Escalated")
    var tabIdx by remember { mutableIntStateOf(0) }
    val statusFilter = listOf("SUBMITTED", "FLAGGED", "ESCALATED")[tabIdx]
    val queue = s.reports.filter { it.status.equals(statusFilter, ignoreCase = true) }

    val user     = s.user
    val zoneName = user?.zone_name ?: "Zone"

    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Zone reports", back = { nav.popBackStack() })
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
            Text(zoneName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
        }
        TabRow(selectedTabIndex = tabIdx) {
            tabs.forEachIndexed { i, t -> Tab(selected = tabIdx == i, onClick = { tabIdx = i }, text = { Text(t) }) }
        }
        Loading(s.busy)

        LazyColumn(Modifier.padding(horizontal = 16.dp)) {
            item {
                Text("Awaiting review", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 10.dp))
            }
            if (queue.isEmpty() && !s.busy) {
                item { EmptyState("No ${tabs[tabIdx].lowercase()} reports") }
            }
            items(queue) { r ->
                Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(r.status)
                            Spacer(Modifier.width(8.dp))
                        }
                        DashRow("R", r.title, "Period: ${r.period} · ${r.submitted_by_name ?: "Submitted by"}")
                        ReportReviewActions(r, vm, nav)
                    }
                }
            }

            // Workflow steps
            item {
                DashSection("Workflow") {
                    DashRow("•", "Review", "Approve, flag or reject")
                    KpnDivider()
                    DashRow("•", "Escalate", "Approved report can move to State")
                }
            }
        }
    }
}

// ── Community Report Review Screen ────────────────────────────────────────────

@Composable
internal fun CommunityReportReviewScreen(vm: AppViewModel, s: AppState, nav: NavHostController) {
    var showDecisionFor by remember { mutableStateOf<CommunityReport?>(null) }
    var action by remember { mutableStateOf("approve") }
    var infoStatus by remember { mutableStateOf("VERIFIED") }
    var internalNotes by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadCommunityReports() }

    showDecisionFor?.let { report ->
        AlertDialog(
            onDismissRequest = { showDecisionFor = null },
            title = { Text("Review Community Report") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Report: ${ContentChoices.label(ContentChoices.communityReports,report.category)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Action:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("approve", "under_review", "reject").forEach { a ->
                            FilterChip(selected = action == a, onClick = { action = a }, label = { Text(a.replace('_', ' ').replaceFirstChar { it.uppercaseChar() }) })
                        }
                    }
                    if (action == "approve") {
                        Text("Info Status:", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("VERIFIED", "DEVELOPING", "UNVERIFIED").forEach { s ->
                                FilterChip(selected = infoStatus == s, onClick = { infoStatus = s }, label = { Text(s) })
                            }
                        }
                    }
                    KpnField("Internal notes (optional)", internalNotes, { internalNotes = it }, lines = 2)
                }
            },
            confirmButton = {
                Button(shape=MaterialTheme.shapes.small, modifier=Modifier.heightIn(min=48.dp), onClick = {
                    vm.reviewCommunityReport(
                        report.id,
                        CommunityReportDecision(action = action, info_status = if (action == "approve") infoStatus else "", internal_notes = internalNotes)
                    ) { showDecisionFor = null }
                }, enabled = !s.busy) { Text("Submit") }
            },
            dismissButton = { OutlinedButton(shape=MaterialTheme.shapes.small, modifier=Modifier.heightIn(min=48.dp), onClick = { showDecisionFor = null }) { Text("Cancel") } }
        )
    }

    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Community Report Review", back = { nav.popBackStack() })
        Loading(s.busy)
        ErrorState(s.errors["communityReports"]) { vm.clearError("communityReports") }

        LazyColumn(Modifier.padding(horizontal = 16.dp)) {
            if (s.communityReports.isEmpty() && !s.busy) {
                item { EmptyState("No community reports to review") }
            }
            items(s.communityReports) { r ->
                Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(ContentChoices.label(ContentChoices.communityReports,r.category), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            StatusBadge(r.status)
                        }
                        Text(r.what_happened,
                            style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                        Text("Reported: ${r.reporter_name} · ${r.incident_date}",
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                        if (r.info_status.isNotBlank()) {
                            Text("Info: ${r.info_status}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 2.dp))
                        }
                        if (r.status == "PENDING" || r.status == "UNDER_REVIEW") {
                            Spacer(Modifier.height(8.dp))
                            Button(shape=MaterialTheme.shapes.small, onClick = { showDecisionFor = r; action = "approve"; infoStatus = "VERIFIED"; internalNotes = "" }, modifier =(Modifier.fillMaxWidth()).heightIn(min=48.dp)) {
                                Text("Review this report")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Announcements Screen ──────────────────────────────────────────────────────

@Composable
internal fun AnnouncementsScreen(vm: AppViewModel, s: AppState, nav: NavHostController, policy: RolePolicy) {
    val canCreate = "CREATE_ANNOUNCEMENT" in s.user?.capabilities.orEmpty()
    var showMine by remember { mutableStateOf(true) }
    var deleting by remember {mutableStateOf<AnnouncementDto?>(null)}
    deleting?.let{announcement->AlertDialog(onDismissRequest={deleting=null},title={Text("Delete announcement?")},text={Text("Remove “${announcement.title}” from the website and app? This cannot be undone from the app.")},confirmButton={TextButton(enabled=!s.busy,onClick={vm.deleteAnnouncement(announcement.id){deleting=null}}){Text("Delete",color=MaterialTheme.colorScheme.error)}},dismissButton={TextButton(onClick={deleting=null}){Text("Cancel")}})}

    LaunchedEffect(showMine) {
        if (showMine && canCreate) vm.loadAnnouncements(mine = true)
        else vm.loadReceivedAnnouncements()
    }

    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Announcements", back = { nav.popBackStack() })
        if (canCreate) {
            TabRow(selectedTabIndex = if (!showMine) 0 else 1) {
                Tab(selected = !showMine, onClick = { showMine = false }, text = { Text("Received") })
                Tab(selected = showMine, onClick = { showMine = true }, text = { Text("My Announcements") })
            }
        }
        Loading(s.busy)
        ErrorState(s.errors["announcements"] ?: s.errors["receivedAnnouncements"]) {
            vm.clearError("announcements"); vm.clearError("receivedAnnouncements")
        }
        val items = if (showMine) s.announcements else s.receivedAnnouncements
        LazyColumn(Modifier.padding(horizontal = 16.dp)) {
            if (canCreate && showMine) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Button(shape=MaterialTheme.shapes.small, onClick = { nav.navigate("create_announcement") }, modifier =(Modifier.fillMaxWidth()).heightIn(min=48.dp)) {
                        Icon(Icons.Filled.Add, null); Spacer(Modifier.width(8.dp)); Text("Create Announcement")
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            if (items.isEmpty() && !s.busy) {
                item { EmptyState(if (showMine) "No announcements created" else "No announcements received") }
            }
            items(items) { a ->
                Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(a.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            StatusBadge(a.priority)
                        }
                        Text(a.content,
                            style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                        Text("Scope: ${a.scope} · ${KpnDates.date(a.created_at)}",
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                        a.expires_at?.let{Text("Expires: $it",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
                        if(showMine&&canCreate)Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){TextButton(enabled=!s.busy,onClick={vm.updateAnnouncement(a.copy(is_active=!a.is_active))}){Text(if(a.is_active)"Deactivate" else "Activate")};TextButton(enabled=!s.busy,onClick={deleting=a}){Text("Delete",color=MaterialTheme.colorScheme.error)}}
                    }
                }
            }
        }
    }
}

// ── Create Announcement Screen ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateAnnouncementScreen(vm:AppViewModel,s:AppState,nav:NavHostController) {
    val user=s.user ?: return
    val scopes=when(user.tier){"STATE"->listOf("GENERAL","STATE","ZONAL","LGA","WARD");"ZONAL"->listOf("ZONAL","LGA","WARD");"LGA"->listOf("LGA","WARD");"WARD"->listOf("WARD");else->emptyList()}
    LaunchedEffect(Unit){vm.loadRegistrationData();if(user.tier=="ZONAL")user.zone?.let{vm.loadLgas(it)};if(user.tier in listOf("LGA","WARD"))user.lga?.let{vm.loadWards(it)}}
    var title by remember{mutableStateOf("")};var content by remember{mutableStateOf("")};var scope by remember{mutableStateOf(scopes.firstOrNull().orEmpty())};var priority by remember{mutableStateOf("NORMAL")}
    var zone by remember{mutableStateOf(if(user.tier!="STATE"&&user.zone!=null)Zone(user.zone,user.zone_name.orEmpty()) else null)}
    var lga by remember{mutableStateOf(if(user.tier in listOf("LGA","WARD")&&user.lga!=null)Lga(user.lga,user.lga_name.orEmpty(),user.zone ?: 0) else null)}
    var ward by remember{mutableStateOf(if(user.tier=="WARD"&&user.ward!=null)Ward(user.ward,user.ward_name.orEmpty(),user.lga ?: 0) else null)}
    var expiry by remember{mutableStateOf<String?>(null)};var choosingExpiry by remember{mutableStateOf(false)};val dateState=rememberDatePickerState()
    if(choosingExpiry)DatePickerDialog(onDismissRequest={choosingExpiry=false},confirmButton={TextButton(enabled=dateState.selectedDateMillis!=null,onClick={val day=java.time.Instant.ofEpochMilli(dateState.selectedDateMillis!!).atZone(java.time.ZoneOffset.UTC).toLocalDate();expiry=day.atTime(23,59).atZone(java.time.ZoneId.of("Africa/Lagos")).toOffsetDateTime().toString();choosingExpiry=false}){Text("Set expiration")}},dismissButton={TextButton(onClick={choosingExpiry=false}){Text("Cancel")}}){DatePicker(state=dateState)}
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Create announcement",back={nav.popBackStack()})
        Column(Modifier.verticalScroll(rememberScrollState()).imePadding().padding(20.dp)) {
            FormSection("01","Announcement");KpnField("Title",title,{title=it});KpnField("Message",content,{content=it},lines=6)
            FormSection("02","Audience","Only scopes permitted by your role are available. The server validates every target.");KpnDropdown("Scope",scope,scopes,{if(it=="STATE")"State executive team" else if(it=="GENERAL")"General — all members" else it.replace('_',' ')},{scope=it})
            if(scope=="STATE")Text("Only verified, active state executive team members receive this announcement.",style=MaterialTheme.typography.bodyMedium,modifier=Modifier.padding(vertical=12.dp))
            if(user.tier=="STATE"&&scope !in listOf("GENERAL","STATE"))KpnDropdown("Senatorial zone",zone,s.zones,{it.name},{zone=it;lga=null;ward=null;vm.loadLgas(it.id)})
            else if(scope !in listOf("GENERAL","STATE"))Text(user.jurisdictionLabel,style=MaterialTheme.typography.bodyMedium,modifier=Modifier.padding(vertical=12.dp))
            if(user.tier in listOf("STATE","ZONAL")&&scope in listOf("LGA","WARD"))KpnDropdown("LGA",lga,s.lgas,{it.name},{lga=it;ward=null;vm.loadWards(it.id)},enabled=zone!=null)
            if(user.tier!="WARD"&&scope=="WARD")KpnDropdown("Ward",ward,s.wards,{it.name},{ward=it},enabled=lga!=null)
            KpnDropdown("Priority",priority,listOf("LOW","NORMAL","HIGH","URGENT"),{it},{priority=it})
            TextButton(onClick={choosingExpiry=true}){Icon(Icons.Default.DateRange,"Set expiration");Spacer(Modifier.width(8.dp));Text(expiry?.let{"Expires after ${it.take(10)} (Nigeria time)"} ?: "Optional expiration date")}
            if(expiry!=null)TextButton(onClick={expiry=null}){Text("Remove expiration")}
            ErrorState(s.errors["announcements"]){vm.clearError("announcements")}
            val targetReady=when(scope){"ZONAL"->zone!=null;"LGA"->lga!=null;"WARD"->ward!=null;else->scope in listOf("GENERAL","STATE")}
            KpnPrimaryButton("Publish announcement",s.busy,title.isNotBlank()&&content.isNotBlank()&&targetReady){vm.createAnnouncement(AnnouncementDto(title=title,content=content,scope=scope,priority=priority,expires_at=expiry,target_zone=if(scope=="ZONAL")zone?.id else null,target_lga=if(scope=="LGA")lga?.id else null,target_ward=if(scope=="WARD")ward?.id else null)){nav.popBackStack()}}
        }
    }
}

// ── Trusted Reporters Screen ──────────────────────────────────────────────────

@Composable
internal fun TrustedReportersScreen(vm: AppViewModel, s: AppState, nav: NavHostController) {
    var showConfirmFor by remember { mutableStateOf<UserDto?>(null) }
    var pendingAction by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadReporters() }

    showConfirmFor?.let { reporter ->
        val label = if (pendingAction == "promote-trusted") "Promote to Trusted Reporter" else "Revoke Trusted Reporter Status"
        AlertDialog(
            onDismissRequest = { showConfirmFor = null },
            title = { Text(label) },
            text = { Text("Are you sure you want to $label for ${reporter.first_name} ${reporter.last_name}?") },
            confirmButton = {
                Button(shape=MaterialTheme.shapes.small, modifier=Modifier.heightIn(min=48.dp), onClick = {
                    vm.reporterAction(reporter.id, pendingAction)
                    showConfirmFor = null
                }, enabled = !s.busy) { Text("Confirm") }
            },
            dismissButton = { OutlinedButton(shape=MaterialTheme.shapes.small, modifier=Modifier.heightIn(min=48.dp), onClick = { showConfirmFor = null }) { Text("Cancel") } }
        )
    }

    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Trusted Reporter System", back = { nav.popBackStack() })
        Loading(s.busy)
        ErrorState(s.errors["reporters"] ?: s.errors["reporterAction"]) {
            vm.clearError("reporters"); vm.clearError("reporterAction")
        }

        val trusted = s.reporters.filter { it.is_trusted_reporter }
        val community = s.reporters.filter { !it.is_trusted_reporter && it.reporter_level == "COMMUNITY_REPORTER" }

        LazyColumn(Modifier.padding(horizontal = 16.dp)) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("Active Trusted Reporters (${trusted.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
            }
            if (trusted.isEmpty() && !s.busy) {
                item { Text("No Trusted Reporters currently assigned.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            items(trusted) { r ->
                Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${r.first_name} ${r.last_name}", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            Icon(Icons.Filled.VerifiedUser, "Verified", tint = MaterialTheme.colorScheme.primary)
                        }
                        Text(r.lga_name ?: r.zone_name ?: "State", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        OutlinedButton(shape=MaterialTheme.shapes.small, 
                            onClick = { showConfirmFor = r; pendingAction = "revoke" },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier =(Modifier.fillMaxWidth()).heightIn(min=48.dp),
                        ) { Text("Revoke Status") }
                    }
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
                Text("Eligible Community Reporters (${community.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
            }
            if (community.isEmpty() && !s.busy) {
                item { Text("No eligible Community Reporters found.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            items(community) { r ->
                Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${r.first_name} ${r.last_name}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(r.lga_name ?: r.zone_name ?: "State", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(shape=MaterialTheme.shapes.small, modifier=Modifier.heightIn(min=48.dp), onClick = { showConfirmFor = r; pendingAction = "promote-trusted" }) { Text("Promote") }
                    }
                }
            }
        }
    }
}

// ── Financial Reports Screen ──────────────────────────────────────────────────

@Composable
internal fun FinancialReportsScreen(vm: AppViewModel, s: AppState, nav: NavHostController) {
    LaunchedEffect(Unit) { vm.loadFinancialReports() }

    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Financial Reports", back = { nav.popBackStack() },actions={if("MANAGE_FINANCIAL_REPORTS" in s.user?.capabilities.orEmpty())IconButton(onClick={nav.navigate("resource/financial-reports")}){Icon(Icons.Default.Add,"Manage financial reports")}})
        Loading(s.busy)
        ErrorState(s.errors["financialReports"]) { vm.clearError("financialReports") }
        LazyColumn(Modifier.padding(horizontal = 16.dp)) {
            if (s.financialReports.isEmpty() && !s.busy) {
                item { EmptyState("No financial reports available") }
            }
            items(s.financialReports) { r ->
                Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(r.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("Period: ${r.report_period}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("Income", style = MaterialTheme.typography.labelSmall)
                                Text("₦${r.total_income}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            Column {
                                Text("Expenses", style = MaterialTheme.typography.labelSmall)
                                Text("₦${r.total_expenses}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        if (r.summary.isNotBlank()) {
                            Text(r.summary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                        }
                        Text("Prepared: ${KpnDates.date(r.created_at)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

// ── Audit Reports Screen ──────────────────────────────────────────────────────

@Composable
internal fun AuditReportsScreen(vm: AppViewModel, s: AppState, nav: NavHostController) {
    LaunchedEffect(Unit) { vm.loadAuditReports() }

    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Audit Reports", back = { nav.popBackStack() },actions={if("MANAGE_AUDIT_REPORTS" in s.user?.capabilities.orEmpty())IconButton(onClick={nav.navigate("resource/audit-reports")}){Icon(Icons.Default.Add,"Manage audit drafts")}})
        Loading(s.busy)
        ErrorState(s.errors["auditReports"]) { vm.clearError("auditReports") }
        LazyColumn(Modifier.padding(horizontal = 16.dp)) {
            if (s.auditReports.isEmpty() && !s.busy) {
                item { EmptyState("No audit reports found") }
            }
            items(s.auditReports) { r ->
                Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(r.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            StatusBadge(r.status)
                        }
                        Text("Period: ${r.audit_period}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
                        Text("Compliance: ${r.compliance_status}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
                        ReadableBody(r.findings)
                        if(r.recommendations.isNotBlank()){SectionHeading("Recommendations");ReadableBody(r.recommendations)}
                        if(r.status=="DRAFT"&&"MANAGE_AUDIT_REPORTS" in s.user?.capabilities.orEmpty())TextButton(onClick={vm.submitAudit(r.id)},enabled=!s.busy){Text("Submit to President")}
                        Text("Created: ${KpnDates.date(r.created_at)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

// ── Community Outreach Screen ─────────────────────────────────────────────────

@Composable
internal fun OutreachScreen(vm: AppViewModel, s: AppState, nav: NavHostController) {
    LaunchedEffect(Unit) { vm.loadOutreach() }

    var org by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var showForm by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Community Outreach", back = { nav.popBackStack() })
        Loading(s.busy)
        if (showForm) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("New Outreach Record", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                KpnField("Organization", org, { org = it })
                KpnField("Contact person", contact, { contact = it })
                KpnField("Purpose", purpose, { purpose = it }, lines = 2)
                KpnField("Date (YYYY-MM-DD)", date, { date = it })
                KpnField("Location", location, { location = it })
                ErrorState(s.errors["outreach"]) { vm.clearError("outreach") }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(shape=MaterialTheme.shapes.small, onClick = { showForm = false }, modifier =(Modifier.weight(1f)).heightIn(min=48.dp)) { Text("Cancel") }
                    Button(shape=MaterialTheme.shapes.small, 
                        onClick = {
                            vm.submitOutreach(
                                CommunityOutreach(
                                    organization = org.trim(),
                                    contact_person = contact.trim(),
                                    purpose = purpose.trim(),
                                    date = date.trim(),
                                    location = location.trim(),
                                ),
                            ) {
                                showForm = false
                                org = ""; contact = ""; purpose = ""; date = ""; location = ""
                            }
                        },
                        enabled = org.isNotBlank() && date.isNotBlank() && !s.busy,
                        modifier =(Modifier.weight(1f)).heightIn(min=48.dp),
                    ) { Text("Save") }
                }
            }
        } else {
            ErrorState(s.errors["outreach"]) { vm.loadOutreach() }
            LazyColumn(Modifier.padding(horizontal = 16.dp)) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Button(shape=MaterialTheme.shapes.small, onClick = { showForm = true }, modifier =(Modifier.fillMaxWidth()).heightIn(min=48.dp)) {
                        Icon(Icons.Filled.Add, null); Spacer(Modifier.width(8.dp)); Text("Log New Outreach")
                    }
                    Spacer(Modifier.height(8.dp))
                }
                if (s.outreach.isEmpty() && !s.busy) {
                    item { EmptyState("No outreach records yet") }
                }
                items(s.outreach) { o ->
                    Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(o.organization, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                                StatusBadge(o.status)
                            }
                            Text("Contact: ${o.contact_person}", style = MaterialTheme.typography.bodySmall)
                            Text("Purpose: ${o.purpose}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
                            Text("Date: ${o.date} · ${o.location}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Change Password Screen ────────────────────────────────────────────────────

@Composable
internal fun ChangePasswordScreen(vm: AppViewModel, s: AppState, nav: NavHostController) {
    var oldPw   by remember { mutableStateOf("") }
    var newPw   by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var done    by remember { mutableStateOf(false) }

    val mismatch = newPw.isNotBlank() && confirm.isNotBlank() && newPw != confirm
    val weak = newPw.isNotBlank() && newPw.length < 8

    if (done) {
        LaunchedEffect(Unit) { nav.popBackStack() }
    }

    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Change Password", back = { nav.popBackStack() })
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Password Requirements", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text("• At least 8 characters", style = MaterialTheme.typography.bodySmall)
                    Text("• Mix of uppercase and lowercase", style = MaterialTheme.typography.bodySmall)
                    Text("• Include numbers and special characters", style = MaterialTheme.typography.bodySmall)
                }
            }
            KpnField("Current Password", oldPw, { oldPw = it }, password = true)
            KpnField("New Password", newPw, { newPw = it }, password = true)
            if (weak) {
                Text("Password must be at least 8 characters", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
            KpnField("Confirm New Password", confirm, { confirm = it }, password = true)
            if (mismatch) {
                Text("Passwords do not match", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
            ErrorState(s.errors["changePassword"]) { vm.clearError("changePassword") }
            Loading(s.busy)
            Button(shape=MaterialTheme.shapes.small, 
                onClick  = { vm.changePassword(oldPw, newPw) { done = true } },
                enabled  = oldPw.isNotBlank() && newPw.isNotBlank() && !mismatch && !weak && !s.busy,
                modifier =(Modifier.fillMaxWidth()).heightIn(min=48.dp),
            ) {
                Icon(Icons.Filled.Lock, null); Spacer(Modifier.width(8.dp)); Text("Change Password")
            }
        }
    }
}
