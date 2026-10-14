package com.kpn.mobile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.kpn.mobile.R
import com.kpn.mobile.core.*

// ── Entry point ────────────────────────────────────────────────────────────────

@Composable
fun KpnApp(vm: AppViewModel, deepLink: String? = null, onDeepLinkHandled:()->Unit = {}) {
    val s by vm.state.collectAsStateWithLifecycle()
    androidx.compose.material3.Surface(color=MaterialTheme.colorScheme.background) {
        when {
            s.booting -> KpnSplash()
            s.user == null -> PublicNav(vm, s)
            else -> key(accessContextKey(s.user!!)) { MemberNav(vm, s, deepLink, onDeepLinkHandled) }
        }
    }
}

// ── Splash ─────────────────────────────────────────────────────────────────────

@Composable
private fun KpnSplash() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.kpn_logo), null, Modifier.size(88.dp))
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
        }
    }
}

// ── Public nav (bottom nav: Home / News / Act / More) ─────────────────────────

@Composable
private fun PublicNav(vm: AppViewModel, s: AppState) {
    val nav = rememberNavController()
    val current by nav.currentBackStackEntryAsState()
    val route   = current?.destination?.route

    val tabs = listOf(
        NavItem("home",          "Home",         Icons.Outlined.Home),
        NavItem("news",          "News",         Icons.Outlined.Article),
        NavItem("opportunities", PUBLIC_OPPORTUNITIES_LABEL, Icons.Outlined.Bolt),
        NavItem("more_public",   "More",         Icons.Outlined.MoreHoriz),
    )

    Scaffold(
        bottomBar = {
            if (route in tabs.map { it.route } || route?.startsWith("article/") == true) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = publicTabSelected(route, tab.route),
                            onClick  = { nav.navigate(tab.route) { popUpTo("home"); launchSingleTop = true } },
                            icon     = { Icon(tab.icon, tab.label) },
                            label    = { Text(tab.label, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                        )
                    }
                }
            }
        },
    ) { pad ->
        NavHost(nav, "home", Modifier.padding(pad)) {
            composable("home")          { HomeScreen(vm, s, nav) }
            composable("news")          { NewsroomScreen(vm, s, nav) }
            composable("opportunities") { OpportunityScreen(vm, s, nav) }
            composable("more_public")   { PublicMoreScreen(vm, s, nav) }
            composable("settings") { SettingsScreen(nav) }
            composable("community")     { CommunityScreen(vm, s, nav) }
            composable("civic")         { CivicScreen(vm, s, nav) }
            composable("advocacy")      { AdvocacyScreen(vm, s, nav) }
            composable("patrons")       { PatronsScreen(vm, s, nav) }
            composable("leadership")    { LeadershipScreen(vm, s, nav) }
            composable("impact")        { ImpactScreen(vm, s, nav) }
            composable("about")         { ContentScreen(vm, s, nav, "about",   "About KPN") }
            composable("conduct")       { ContentScreen(vm, s, nav, "code",    "Code of Conduct") }
            composable("support")       { ContentScreen(vm, s, nav, "support", "Support KPN") }
            composable("contact")       { ContactScreen(vm, s, nav) }
            composable("faq")           { FaqScreen(vm, s, nav) }
            composable("gallery")       { GalleryScreen(vm, s, nav) }
            composable("login")         { LoginScreen(vm, nav) }
            composable("register")      { RegisterScreen(vm, s, nav) }
            composable("forgot_password") { ForgotPasswordScreen(vm, nav) }
            composable(
                "article/{slug}",
                arguments = listOf(navArgument("slug") { type = NavType.StringType }),
            ) {
                val slug = it.arguments?.getString("slug").orEmpty()
                ArticleDetailScreen(vm, s, slug, nav)
            }
        }
    }
}

// ── Member nav ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberNav(vm:AppViewModel,s:AppState,deepLink:String?,onDeepLinkHandled:()->Unit) {
    val user=s.user ?: return
    val nav=rememberNavController();val policy=RolePolicies.fromUser(user)
    val current by nav.currentBackStackEntryAsState();val route=current?.destination?.route
    val tabs=listOf(NavItem("home","Home",Icons.Outlined.Home),NavItem("dashboard","Overview",Icons.Outlined.Dashboard),NavItem("workspace","My work",Icons.Outlined.WorkOutline),NavItem("news_member","News",Icons.Outlined.Article),NavItem("more","Account",Icons.Outlined.AccountCircle))

    LaunchedEffect(deepLink){if(!deepLink.isNullOrBlank()){if(WorkspaceActions.canNavigate(user,deepLink))nav.navigate(deepLink){launchSingleTop=true};onDeepLinkHandled()}}
    Scaffold(
        topBar={Column {TopAppBar(title={Row(verticalAlignment=Alignment.CenterVertically){IconButton(modifier=Modifier.sizeIn(minWidth=48.dp,minHeight=48.dp),onClick={nav.navigate("home"){popUpTo(nav.graph.startDestinationId){saveState=true};launchSingleTop=true;restoreState=true}}){Image(painterResource(R.drawable.kpn_logo),"Go to Home",Modifier.size(30.dp))};Spacer(Modifier.width(10.dp));Column{Text("KPN",style=MaterialTheme.typography.titleMedium);Text(memberHeaderTierLabel(user.tier),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}},actions={
            BadgedBox(badge={s.unreadCount?.takeIf{it>0}?.let{Badge{Text(if(it>99)"99+" else it.toString())}}}){IconButton(onClick={nav.navigate("notifications"){launchSingleTop=true}}){Icon(Icons.Outlined.Notifications,"Notifications${s.unreadCount?.let{" · $it unread"}.orEmpty()}")}}
            IconButton(onClick={nav.navigate("profile"){launchSingleTop=true}}){KpnAvatar(user.photo,user.displayName,32)}
        },colors=TopAppBarDefaults.topAppBarColors(containerColor=MaterialTheme.colorScheme.surface));KpnDivider()}},
        bottomBar={if(route in tabs.map{it.route})NavigationBar(containerColor=MaterialTheme.colorScheme.surface,tonalElevation=0.dp){tabs.forEach{tab->NavigationBarItem(selected=route==tab.route,onClick={nav.navigate(tab.route){popUpTo(nav.graph.startDestinationId){saveState=true};launchSingleTop=true;restoreState=true}},icon={Icon(tab.icon,tab.label)},label={Text(tab.label,maxLines=2,overflow=TextOverflow.Ellipsis)})}}}
    ){padding->
        CompositionLocalProvider(LocalMemberShell provides true) {
        NavHost(nav,"dashboard",Modifier.padding(padding)) {
            composable("home"){HomeScreen(vm,s,nav)}
            composable("dashboard"){
                when {
                    user.status!="VERIFIED"->PendingScreen(user,vm::logout)
                    user.access.telegram_required&&!user.access.telegram_verified->TelegramGateScreen(vm,s.roleTitle)
                    else->DashboardRouter(vm,s,nav,policy)
                }
            }
            composable("workspace"){WorkspaceScreen(vm,s,nav)}
            composable("news_member"){NewsroomScreen(vm,s,nav)}
            composable("opportunities_member"){OpportunityScreen(vm,s,nav)}
            composable("more"){MoreScreen(vm,s,nav,policy)}
            composable("settings"){SettingsScreen(nav)}
            composable("profile"){ProfileScreen(vm,s,nav)}
            composable("notifications"){NotificationsScreen(vm,s,nav)}
            composable("received_announcements"){ReceivedAnnouncementsScreen(vm,s,nav)}
            composable("change_password"){ChangePasswordScreen(vm,s,nav)}
            composable("article/{slug}"){ArticleDetailScreen(vm,s,it.arguments?.getString("slug").orEmpty(),nav)}
            composable("member_admin/{id}"){GuardRoute(user,"member_admin/"+it.arguments?.getString("id")){MemberAdministrationScreen(vm,s,nav,it.arguments?.getString("id")?.toIntOrNull() ?: 0)}}
            composable("mobilization"){GuardRoute(user,"mobilization"){MobilizationScreen(vm,s,nav)}}
            composable("members"){GuardRoute(user,"members"){MembersScreen(vm,s,nav,policy)}}
            composable("reports"){GuardRoute(user,"reports"){ReportsScreen(vm,s,nav,policy)}}
            composable("submit_report"){GuardRoute(user,"submit_report"){ReportSubmitScreen(vm,s,nav)}}
            composable("events"){GuardRoute(user,"events"){EventsScreen(vm,s,nav,"MANAGE_EVENTS" in user.capabilities)}}
            composable("event/{id}"){GuardRoute(user,"event/"+it.arguments?.getString("id")){EventDetailScreen(vm,s,nav,it.arguments?.getString("id")?.toIntOrNull() ?: 0)}}
            composable("attendance"){GuardRoute(user,"attendance"){EventAttendanceScreen(vm,s,nav)}}
            composable("create_event"){GuardRoute(user,"create_event"){EventCreateScreen(vm,s,nav)}}
            composable("ward_meetings"){GuardRoute(user,"ward_meetings"){WardMeetingsScreen(vm,s,nav)}}
            composable("ward_meeting_form"){GuardRoute(user,"ward_meeting_form"){WardMeetingFormScreen(vm,s,nav)}}
            composable("editorial"){GuardRoute(user,"editorial"){EditorialScreen(vm,s,nav)}}
            composable("compose_article"){GuardRoute(user,"compose_article"){ArticleComposerScreen(vm,s,nav)}}
            composable("edit_article/{id}"){GuardRoute(user,"edit_article/"+it.arguments?.getString("id")){ArticleComposerScreen(vm,s,nav,it.arguments?.getString("id")?.toIntOrNull())}}
            composable("my_articles"){GuardRoute(user,"my_articles"){MyArticlesScreen(vm,s,nav)}}
            composable("media"){GuardRoute(user,"media_upload"){MediaScreen(vm,s,nav,false)}}
            composable("media_review"){GuardRoute(user,"media_review"){MediaScreen(vm,s,nav,true)}}
            composable("media_upload"){GuardRoute(user,"media_upload"){MediaUploadScreen(vm,s,nav)}}
            composable("programs/{kind}"){val kind=it.arguments?.getString("kind") ?: "youth";GuardRoute(user,"programs/$kind"){ProgramsScreen(vm,s,nav,kind)}}
            composable("create_program/{kind}"){val kind=it.arguments?.getString("kind") ?: "youth";GuardRoute(user,"create_program/$kind"){ProgramCreateScreen(vm,s,nav,kind)}}
            composable("finance"){GuardRoute(user,"finance"){FinanceScreen(vm,s,nav)}}
            composable("discipline"){GuardRoute(user,"discipline"){DisciplineScreen(vm,s,nav)}}
            composable("community_report"){GuardRoute(user,"community_report"){CommunityReportScreen(vm,s,nav)}}
            composable("community_report_review"){GuardRoute(user,"community_report_review"){CommunityReportReviewScreen(vm,s,nav)}}
            composable("announcements"){GuardRoute(user,"announcements"){AnnouncementsScreen(vm,s,nav,policy)}}
            composable("create_announcement"){GuardRoute(user,"announcements"){CreateAnnouncementScreen(vm,s,nav)}}
            composable("trusted_reporters"){GuardRoute(user,"trusted_reporters"){TrustedReportersScreen(vm,s,nav)}}
            composable("financial_reports"){GuardRoute(user,"financial_reports"){FinancialReportsScreen(vm,s,nav)}}
            composable("audit_reports"){GuardRoute(user,"audit_reports"){AuditReportsScreen(vm,s,nav)}}
            composable("outreach"){GuardRoute(user,"outreach"){OutreachScreen(vm,s,nav)}}
            composable("management"){GuardRoute(user,"management"){ManagementHomeScreen(vm,s,nav)}}
            composable("resource/{kind}"){val kind=it.arguments?.getString("kind").orEmpty();GuardRoute(user,"resource/$kind"){ResourceScreen(vm,s,nav,kind)}}
            composable("community"){CommunityScreen(vm,s,nav)}
            composable("civic"){CivicScreen(vm,s,nav)}
            composable("advocacy"){AdvocacyScreen(vm,s,nav)}
            composable("patrons"){PatronsScreen(vm,s,nav)}
            composable("leadership"){LeadershipScreen(vm,s,nav)}
            composable("impact"){ImpactScreen(vm,s,nav)}
            composable("about"){ContentScreen(vm,s,nav,"about","About KPN")}
            composable("conduct"){ContentScreen(vm,s,nav,"code","Code of conduct")}
            composable("support"){ContentScreen(vm,s,nav,"support","Support KPN")}
            composable("contact"){ContactScreen(vm,s,nav)}
            composable("faq"){FaqScreen(vm,s,nav)}
            composable("gallery"){GalleryScreen(vm,s,nav)}
        }
        }
    }
}
@Composable private fun GuardRoute(user:com.kpn.mobile.data.UserDto,route:String,content: @Composable () -> Unit) {
    if(WorkspaceActions.canNavigate(user,route))content() else CenteredMessage("This action is not assigned to your current KPN role.")
}

// ── Bottom nav items per role ──────────────────────────────────────────────────

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

@Composable
private fun EventCreateScreen(vm: AppViewModel, s: AppState, nav: NavHostController) {
    var title       by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var description by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var location    by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var startDate   by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var endDate     by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Create event", back = { nav.popBackStack() })
        Column(
            Modifier
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(16.dp),
        ) {
            KpnField("Event title", title, { title = it })
            KpnField("Description", description, { description = it }, lines = 3)
            KpnField("Location / venue", location, { location = it })
            KpnDateField("Start date",startDate,{startDate=it},dateTime=true,error=s.errors["create_event"]?.fields?.get("start_date"))
            KpnDateField("End date",endDate,{endDate=it},dateTime=true,error=s.errors["create_event"]?.fields?.get("end_date"))
            val validSchedule=runCatching{java.time.OffsetDateTime.parse(endDate).isAfter(java.time.OffsetDateTime.parse(startDate))}.getOrDefault(false)
            if(startDate.isNotBlank()&&endDate.isNotBlank()&&!validSchedule)Text("End date and time must be after the start.",color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)
            ErrorState(s.errors["create_event"]) { vm.clearError("create_event") }
            Loading(s.busy)
            Button(shape=MaterialTheme.shapes.small, 
                onClick  = { vm.createEvent(title, description, location, startDate, endDate) { if (it) nav.popBackStack() } },
                enabled  = title.isNotBlank() && description.isNotBlank() && location.isNotBlank() && validSchedule && !s.busy,
                modifier =(Modifier.fillMaxWidth()).heightIn(min=48.dp),
            ) { Text("Create event") }
        }
    }
}
