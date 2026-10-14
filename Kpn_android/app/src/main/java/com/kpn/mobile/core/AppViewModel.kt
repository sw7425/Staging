package com.kpn.mobile.core

import android.app.Application
import android.net.Uri
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.kpn.mobile.BuildConfig
import com.kpn.mobile.KpnApplication
import com.kpn.mobile.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.joinAll
import java.util.UUID

data class AppState(
    val loading:Set<String> = emptySet(),
    val homeArticles:List<Article> = emptyList(),
    val homeOpportunities:List<Opportunity> = emptyList(),
    val homeContent: HomeContent? = null,
    val publicPerson: PublicPerson? = null,
    val unreadCount: Int? = null,
    val hasMore: Map<String, Boolean> = emptyMap(),
    val resourceCatalog: List<ResourceSpec> = emptyList(),
    val resourceRows: List<Map<String, Any?>> = emptyList(),
    val minutes: MeetingMinutes? = null,
    val attendance: List<AttendanceRecord> = emptyList(),
    val participantIds: List<Int> = emptyList(),
    val booting: Boolean = true,
    val busy: Boolean = false,
    val user: User? = null,
    val roleTitle: String? = null,
    val errors: Map<String, Result.Err> = emptyMap(),
    // Public content
    val articles: List<Article> = emptyList(),
    val opportunities: List<Opportunity> = emptyList(),
    val impact: Impact? = null,
    val initiatives: List<Initiative> = emptyList(),
    val civic: List<Article> = emptyList(),
    val advocacy: List<Advocacy> = emptyList(),
    val patrons: List<Patron> = emptyList(),
    val leadership: List<LeadershipSeat> = emptyList(),
    val contentPage: ContentPage? = null,
    val faqs: List<FaqItem> = emptyList(),
    val gallery: List<MediaItem> = emptyList(),
    val contactSent: Boolean = false,
    // Auth / registration
    val roles: List<RoleDefinition> = emptyList(),
    val zones: List<Zone> = emptyList(),
    val lgas: List<Lga> = emptyList(),
    val wards: List<Ward> = emptyList(),
    val passwordResetSent: Boolean = false,
    val telegramLink: String? = null,
    // Authenticated content
    val dashboard: DashboardData? = null,
    val members: List<Member> = emptyList(),
    val selectedMember: Member? = null,
    val mobilizationMembers:List<Member> = emptyList(),
    val mobilizationFields:List<ResourceField> = emptyList(),
    val memberAdministration:MemberAdministrationForm?=null,
    val reports: List<ReportItem> = emptyList(),
    val selectedReport: ReportItem? = null,
    val events: List<EventItem> = emptyList(),
    val selectedEvent: EventItem? = null,
    val wardMeetings: List<WardMeeting> = emptyList(),
    val notifications: List<NotificationItem> = emptyList(),
    val programs: List<ProgramItem> = emptyList(),
    val finance: FinanceSummary? = null,
    val donations: List<Donation> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val disciplines: List<Discipline> = emptyList(),
    val media: List<MediaItem> = emptyList(),
    val editorialQueue: List<Article> = emptyList(),
    val selectedArticle: Article? = null,
    val announcements: List<AnnouncementDto> = emptyList(),
    val receivedAnnouncements: List<AnnouncementDto> = emptyList(),
    val communityReports: List<CommunityReport> = emptyList(),
    val reporters: List<UserDto> = emptyList(),
    val financialReports: List<FinancialReportDto> = emptyList(),
    val auditReports: List<AuditReportDto> = emptyList(),
    val outreach: List<CommunityOutreach> = emptyList(),
)

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val application = app as KpnApplication
    private val api         = application.api.api
    private val client      = application.api
    private val store       = application.session

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state


    private val readFreshness=ReadFreshness()
    private val readJobs=mutableMapOf<String,Pair<String,Job>>()
    private var lastAccountCheck=0L
    private fun cachedRead(scope:String,key:String=scope,force:Boolean=false,ttl:Long=60_000,block:suspend()->Boolean):Job {
        readJobs[scope]?.takeIf{it.first==key && it.second.isActive}?.let{return it.second}
        if(!readFreshness.shouldFetch(scope,key,ttl,force))return viewModelScope.launch{}
        readJobs[scope]?.second?.cancel()
        update{it.copy(loading=it.loading+scope,errors=it.errors-scope)}
        val job=viewModelScope.launch {
            try { if(block())readFreshness.succeeded(scope,key) }
            finally { if(readJobs[scope]?.second==currentCoroutineContext()[Job]||readJobs[scope]==null)update{it.copy(loading=it.loading-scope)} }
        }
        readJobs[scope]=key to job
        job.invokeOnCompletion{if(readJobs[scope]?.second==job)readJobs.remove(scope)}
        return job
    }

    // ── State helpers ─────────────────────────────────────────────────────────

    private fun update(f: (AppState) -> AppState) { _state.value = f(_state.value) }
    private fun mutationUpdate(f:(AppState)->AppState){readFreshness.clear();update(f)}
    private fun fail(scope: String, e: Result.Err) {
        if(e.status==401 && store.access()==null) { deviceRegistrationUser=null;cancelOtherRequests(null) }
        update { stateAfterFailure(it,scope,e,store.access()) }
    }
    private fun clear(scope: String)                = update { it.copy(errors = it.errors - scope) }

    private var deviceRegistrationUser:Int?=null
    private var bootJob:Job?=null
    private var deviceRegistrationJob:Job?=null
    private var notificationObserverJob:Job?=null
    private var signingOut=false
    private var retirementJob:Job?=null
    init { boot();notificationObserverJob=viewModelScope.launch { application.notificationEvents.updates.collect { if(!signingOut && store.access()!=null) { boot(force=true).join();loadUnreadCount(force=true);if(_state.value.user?.status=="VERIFIED" && _state.value.dashboard!=null) loadDashboard(force=true) } } } }
    private fun cancelOtherRequests(current:Job?) {
        readFreshness.clear();lastAccountCheck=0L
        viewModelScope.coroutineContext[Job]?.children?.toList()?.forEach { if(it!=current && it!=notificationObserverJob && it!=retirementJob) it.cancel() }
    }
    private suspend fun resolveUser(u: User) {
        if(signingOut) return
        val previous=_state.value.user
        if(previous!=null && !sameAccessContext(previous,u)) { cancelOtherRequests(currentCoroutineContext()[Job]);_state.value=AppState(booting=false) }
        store.saveUserId(u.id)
        update { it.copy(booting = false, busy = false, user = u, roleTitle = u.role_title, errors = it.errors - "auth" - "login" - "boot") }
        lastAccountCheck=System.nanoTime()/1_000_000
        loadUnreadCount()
        if(deviceRegistrationUser!=u.id) registerCurrentDevice() // Pending members also receive approval/status notifications.
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

    fun boot(force:Boolean=false):Job {
        bootJob?.takeIf{it.isActive}?.let{return it}
        val account=_state.value.user
        if(!force && account?.status=="VERIFIED" && System.nanoTime()/1_000_000-lastAccountCheck<15_000)return viewModelScope.launch{}
        bootJob?.cancel()
        return viewModelScope.launch {
        if(signingOut) return@launch
        if (store.access() == null) { _state.value=AppState(booting=false); return@launch }
        when (val r = client.safe { api.me() }) {
            is Result.Ok  -> resolveUser(r.value)
            is Result.Err -> { if (r.status == 401) store.clear(); fail("boot", r) }
        }
        }.also { bootJob=it }
    }

    private var loginJob:Job?=null
    fun login(username: String, password: String):Job {
        loginJob?.cancel()
        return viewModelScope.launch {
        if(signingOut) return@launch
        cancelOtherRequests(currentCoroutineContext()[Job])
        deviceRegistrationUser=null
        _state.value=AppState(booting=false,busy=true)
        when (val r = client.safe { api.login(LoginRequest(username.trim(), password)) }) {
            is Result.Ok  -> {
                store.save(r.value.access, r.value.refresh)
                when (val me = client.safe { api.me() }) {
                    is Result.Ok  -> resolveUser(me.value)
                    is Result.Err -> fail("login", me)
                }
            }
            is Result.Err -> fail("login", r)
        }
        }.also { loginJob=it }
    }

    fun logout() = viewModelScope.launch {
        if(signingOut) return@launch
        signingOut=true
        cancelOtherRequests(currentCoroutineContext()[Job])
        bootJob?.cancel();deviceRegistrationJob?.cancel();deviceRegistrationUser=null
        val tokens=synchronized(store){RetirementTokens(store.access(),store.refresh())}
        val deviceToken=store.deviceToken()
        _state.value=AppState(booting=true)
        // Local revocation is durable before Guest UI. Network retirement never owns the real store.
        withContext(NonCancellable+Dispatchers.IO){store.clearDurably()}
        _state.value=AppState(booting=false)
        signingOut=false
        retirementJob=viewModelScope.launch {
            val retiringClient=ApiClient(tokens)
            withTimeoutOrNull(8_000) {
                deviceToken?.let { retiringClient.safe { retiringClient.api.deactivateDevice(DeviceDeactivateRequest(it)) } }
                tokens.refresh()?.let { retiringClient.safe { retiringClient.api.logout(LogoutRequest(it)) } }
            }
            tokens.clearTokens()
        }
    }

    fun clearError(scope: String) = clear(scope)

    fun requestPasswordReset(email: String) = viewModelScope.launch {
        update { it.copy(busy = true, passwordResetSent = false, errors = it.errors - "password_reset") }
        when (val r = client.safe { api.requestPasswordReset(PasswordResetRequest(email.trim())) }) {
            is Result.Ok  -> update { it.copy(busy = false, passwordResetSent = true) }
            is Result.Err -> fail("password_reset", r)
        }
    }

    // ── Registration ──────────────────────────────────────────────────────────

    private suspend fun <T> allPages(fetch: suspend (Int) -> Page<T>): List<T> {
        val rows = mutableListOf<T>(); var page = 1
        while (true) { val response = fetch(page); rows += response.results; if (response.next == null) break; page++; check(page <= 200) { "Too many pages; narrow your filters." } }
        return rows
    }
    fun loadRegistrationData() = viewModelScope.launch {
        update { it.copy(busy = true, errors = it.errors - "register_data") }
        when(val r = client.safe { allPages { api.zones(it) } }) {
            is Result.Ok -> update { it.copy(busy = false, zones = r.value, roles = emptyList()) }
            is Result.Err -> fail("register_data", r)
        }
    }
    fun loadVacancies(zone: Int?, lga: Int?, ward: Int?) = latest("vacancies") {
        update { it.copy(roles = emptyList()) }
        when(val r = client.safe { api.vacantRoles(zone,lga,ward) }) {
            is Result.Ok -> update { it.copy(roles = r.value.vacant_roles) }
            is Result.Err -> fail("register_data",r)
        }
    }
    fun loadLgas(zoneId: Int) = latest("lgas") {
        publicRequests["wards"]?.cancel()
        update { it.copy(lgas = emptyList(), wards = emptyList()) }
        when(val r = client.safe { allPages { api.lgas(zoneId,it) } }) { is Result.Ok -> update { it.copy(lgas = r.value) }; is Result.Err -> fail("locations",r) }
    }
    fun loadWards(lgaId: Int) = latest("wards") {
        update { it.copy(wards = emptyList()) }
        when(val r = client.safe { allPages { api.wards(lgaId,it) } }) { is Result.Ok -> update { it.copy(wards = r.value) }; is Result.Err -> fail("locations",r) }
    }
    fun register(body: RegistrationRequest, photo: Uri) = viewModelScope.launch {
        update { it.copy(busy = true, errors = it.errors - "register") }
        when(val r = client.safe {
            val json = client.moshi.adapter(RegistrationRequest::class.java).toJson(body)
            @Suppress("UNCHECKED_CAST") val values = client.moshi.adapter(Map::class.java).fromJson(json) as Map<String,Any?>
            val fields = values.filterValues { it != null }.mapValues { (_, value) -> (if(value is Double && value % 1.0 == 0.0) value.toInt().toString() else value.toString()).toRequestBody("text/plain".toMediaType()) }
            api.registerPhoto(fields, uploadPart(application,photo,"photo",photo = true))
        }) { is Result.Ok -> update { it.copy(busy = false, user = r.value, roleTitle = r.value.role_title) }; is Result.Err -> fail("register",r) }
    }

    // ── Public content ────────────────────────────────────────────────────────

    fun loadHome(force:Boolean=false) = viewModelScope.launch {
        joinAll(
            cachedRead("home",force=force) {
                when(val r=client.safe{api.homeContent()}) {is Result.Ok->{update{it.copy(homeContent=r.value)};true};is Result.Err->{fail("home",r);false}}
            },
            cachedRead("home_news",force=force) {
                when(val r=client.safe{api.articles()}) {is Result.Ok->{update{it.copy(homeArticles=r.value.results)};true};is Result.Err->{fail("home_news",r);false}}
            },
            cachedRead("home_opportunities",force=force) {
                when(val r=client.safe{api.opportunities()}) {is Result.Ok->{update{it.copy(homeOpportunities=r.value.results)};true};is Result.Err->{fail("home_opportunities",r);false}}
            }
        )
    }
    private val publicPages = mutableMapOf<String,Int>()
    private val publicRequests=mutableMapOf<String,Job>()
    private fun latest(scope:String,block:suspend()->Unit):Job {
        publicRequests[scope]?.cancel()
        return viewModelScope.launch{block()}.also{publicRequests[scope]=it}
    }
    fun loadNews(query:String="",category:String?=null,more:Boolean=false,force:Boolean=false):Job {
        val page=if(more)(publicPages["news"] ?: 1)+1 else 1
        return cachedRead("news","$query|$category|$page",force||more) {
            when(val r=client.safe{api.articles(page,category,query.ifBlank{null})}) {
                is Result.Ok->{publicPages["news"]=page;update{it.copy(articles=if(more)it.articles+r.value.results else r.value.results,hasMore=it.hasMore+("news" to (r.value.next!=null)))};true}
                is Result.Err->{fail("news",r);false}
            }
        }
    }
    fun loadOpportunities(category:String?=null,more:Boolean=false,force:Boolean=false):Job {
        val page=if(more)(publicPages["opportunities"] ?: 1)+1 else 1
        return cachedRead("opportunities","$category|$page",force||more) {
            when(val r=client.safe{api.opportunities(page,category)}) {
                is Result.Ok->{publicPages["opportunities"]=page;update{it.copy(opportunities=if(more)it.opportunities+r.value.results else r.value.results,hasMore=it.hasMore+("opportunities" to (r.value.next!=null)))};true}
                is Result.Err->{fail("opportunities",r);false}
            }
        }
    }
    fun loadLeadership(tier:String?=null,zone:Int?=null,lga:Int?=null,ward:Int?=null,more:Boolean=false,force:Boolean=false):Job {
        val page=if(more)(publicPages["leadership"] ?: 1)+1 else 1
        return cachedRead("leadership","$tier|$zone|$lga|$ward|$page",force||more) {
            when(val r=client.safe{api.leadershipSeats(page,tier,"FILLED",zone,lga,ward)}) {
                is Result.Ok->{publicPages["leadership"]=page;update{it.copy(leadership=if(more)it.leadership+r.value.results else r.value.results,hasMore=it.hasMore+("leadership" to (r.value.next!=null)))};true}
                is Result.Err->{fail("leadership",r);false}
            }
        }
    }
    fun loadPublicPerson(id:Int) = viewModelScope.launch {
        update { it.copy(publicPerson=null) }
        when(val r=client.safe { api.publicPerson(id) }) { is Result.Ok -> update { it.copy(publicPerson=r.value) }; is Result.Err -> fail("person",r) }
    }

    fun loadArticle(slug: String) = viewModelScope.launch {
        update { it.copy(busy = true, selectedArticle = null, errors = it.errors - "article") }
        when (val r = client.safe { api.article(slug) }) {
            is Result.Ok  -> update { it.copy(busy = false, selectedArticle = r.value) }
            is Result.Err -> fail("article", r)
        }
    }

    fun loadImpact(force:Boolean=false)=cachedRead("impact",force=force){
        when(val r=client.safe{api.impact()}){is Result.Ok->{update{it.copy(impact=r.value)};true};is Result.Err->{fail("impact",r);false}}
    }
    fun loadPublicSection(section:String,more:Boolean=false,force:Boolean=false):Job {
        if(section=="leadership")return loadLeadership(force=force)
        val page=if(more)(publicPages[section] ?: 1)+1 else 1
        return cachedRead(section,"$section|$page",force||more) {
            val r=when(section) {
                "community"->client.safe{api.initiatives(page)}
                "civic"->client.safe{api.civic(page)}
                "advocacy"->client.safe{api.advocacy(page)}
                "patrons"->client.safe{api.patrons(page)}
                else->return@cachedRead false
            }
            when(r){
                is Result.Ok->{
                    publicPages[section]=page
                    update { state ->
                        val rows=r.value.results
                        val base=state.copy(hasMore=state.hasMore+(section to (r.value.next!=null)))
                        @Suppress("UNCHECKED_CAST")
                        when(section){
                            "community"->base.copy(initiatives=if(more)base.initiatives+(rows as List<Initiative>) else rows as List<Initiative>)
                            "civic"->base.copy(civic=if(more)base.civic+(rows as List<Article>) else rows as List<Article>)
                            "advocacy"->base.copy(advocacy=if(more)base.advocacy+(rows as List<Advocacy>) else rows as List<Advocacy>)
                            "patrons"->base.copy(patrons=if(more)base.patrons+(rows as List<Patron>) else rows as List<Patron>)
                            else->base
                        }
                    };true
                }
                is Result.Err->{fail(section,r);false}
            }
        }
    }

    private var contentKey:String?=null
    fun loadContent(section:String,force:Boolean=false)=cachedRead("content",section,force||contentKey!=section) {
        update{it.copy(contentPage=if(contentKey==section)it.contentPage else null,errors=it.errors-section)}
        when(val r=client.safe {
            when(section){"about"->api.about();"contact"->api.contact();"support"->api.support();"code"->api.codeOfConduct();"faq"->api.faq();"gallery"->api.gallery();else->throw IllegalArgumentException("Unknown public content route")}
        }){
            is Result.Ok->{
                contentKey=section
                update { state -> when(section){
                    "faq"->{@Suppress("UNCHECKED_CAST") val page=r.value as Page<FaqItem>;state.copy(faqs=page.results,hasMore=state.hasMore+("faq" to (page.next!=null)))}
                    "gallery"->{@Suppress("UNCHECKED_CAST") val page=r.value as Page<MediaItem>;state.copy(gallery=page.results,hasMore=state.hasMore+("gallery" to (page.next!=null)))}
                    else->state.copy(contentPage=r.value as ContentPage)
                } };true
            }
            is Result.Err->{fail(section,r);false}
        }
    }

    fun sendContact(name: String, email: String, message: String) = viewModelScope.launch {
        update { it.copy(busy = true, contactSent = false) }
        when (val r = client.safe { api.sendContact(ContactRequest(name, email, message)) }) {
            is Result.Ok  -> update { it.copy(busy = false, contactSent = true) }
            is Result.Err -> fail("contact", r)
        }
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────

    fun loadDashboard(force:Boolean=false)=cachedRead("dashboard",force=force,ttl=30_000) {
        when(val r=client.safe{api.myDashboard()}){is Result.Ok->{update{it.copy(dashboard=r.value)};true};is Result.Err->{fail("dashboard",r);false}}
    }

    // ── Members ───────────────────────────────────────────────────────────────

    fun loadMembers(pending: Boolean = false) = viewModelScope.launch {
        update { it.copy(busy = true) }
        when (val r = client.safe { Page(results=allPages { if(pending) api.pendingMembers(it) else api.members(it) }) }) {
            is Result.Ok  -> update { it.copy(busy = false, members = r.value.results) }
            is Result.Err -> fail("members", r)
        }
    }

    fun loadMember(id: Int) = viewModelScope.launch {
        update { it.copy(selectedMember=null,errors=it.errors-"member_detail") }
        when (val r = client.safe { api.member(id) }) {
            is Result.Ok  -> update { it.copy(selectedMember = r.value) }
            is Result.Err -> fail("member_detail", r)
        }
    }

    fun decideMember(id: Int, decision: String, pending: Boolean = true, onDone:()->Unit={}) = viewModelScope.launch {
        // A decision-specific flag cannot be cleared by an unrelated read's global busy update.
        if ("member_decision" in _state.value.loading) return@launch
        mutationUpdate { it.copy(busy=true,loading=it.loading+"member_decision",errors=it.errors-"member_decision") }
        try {
            val response = client.safe { api.decideMember(id, MemberDecision(decision)) }
            val result = when(response) {
                is Result.Ok -> confirmedMemberDecision(id,decision,response.value)
                is Result.Err -> response
            }
            when(result) {
                is Result.Ok -> {
                    mutationUpdate { stateAfterConfirmedMemberDecision(it,result.value) }
                    onDone()
                    loadMembers(pending)
                    loadDashboard(force=true)
                    loadUnreadCount(force=true)
                }
                is Result.Err -> fail("member_decision",result)
            }
        } finally { update { it.copy(loading=it.loading-"member_decision") } }
    }

    private var mobilizationPage=1
    fun loadMobilizationFilters(filters:Map<String,String> = emptyMap())=latest("mobilization_filters") {
        update{it.copy(errors=it.errors-"mobilization_filters")}
        when(val r=client.safe{api.mobilizationFilters(filters.filterValues{it.isNotBlank()})}){is Result.Ok->update{it.copy(mobilizationFields=r.value.fields)};is Result.Err->fail("mobilization_filters",r)}
    }
    fun loadMobilization(filters:Map<String,String>,more:Boolean=false)=latest("mobilization") {
        val page=if(more)mobilizationPage+1 else 1
        update{it.copy(busy=true,errors=it.errors-"mobilization",mobilizationMembers=if(more)it.mobilizationMembers else emptyList())}
        when(val r=client.safe{api.mobilization(filters.filterValues{it.isNotBlank()},page)}){
            is Result.Ok->{mobilizationPage=page;update{it.copy(busy=false,mobilizationMembers=if(more)(it.mobilizationMembers+r.value.results).distinctBy{row->row.id} else r.value.results,hasMore=it.hasMore+("mobilization" to (r.value.next!=null)))}}
            is Result.Err->fail("mobilization",r)
        }
    }
    fun exportMobilization(format:String,filters:Map<String,String>,destination:Uri,onDone:()->Unit)=viewModelScope.launch {
        update{it.copy(busy=true,errors=it.errors-"mobilization_export")}
        when(val r=client.safe{val body=api.mobilizationExport(format,filters.filterValues{it.isNotBlank()});kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO){body.use{response->application.contentResolver.openOutputStream(destination)?.use{output->response.byteStream().use{input->input.copyTo(output)}} ?: error("Unable to write the selected document.")}}}){
            is Result.Ok->{update{it.copy(busy=false)};onDone()};is Result.Err->fail("mobilization_export",r)
        }
    }

    fun loadMemberAdministration(id:Int,action:String,values:Map<String,String> = emptyMap()) = latest("member_admin") {
        update{it.copy(errors=it.errors-"member_admin")}
        when(val r=client.safe{api.memberAdministration(id,action,values.filterKeys{it in setOf("zone","lga","new_role")})}){is Result.Ok->update{it.copy(memberAdministration=r.value)};is Result.Err->fail("member_admin",r)}
    }
    fun administerMember(id:Int,action:String,values:Map<String,String>,onDone:()->Unit) = viewModelScope.launch {
        mutationUpdate{it.copy(busy=true,errors=it.errors-"member_admin")}
        when(val r=client.safe{api.administerMember(id,values+("action" to action))}){
            is Result.Ok->{readFreshness.clear();mutationUpdate{it.copy(busy=false,selectedMember=r.value)};if(_state.value.user?.id==id)boot();onDone()}
            is Result.Err->fail("member_admin",r)
        }
    }
    fun exportMembers(format:String,destination:Uri,onDone:()->Unit) = viewModelScope.launch {
        update{it.copy(busy=true,errors=it.errors-"member_export")}
        when(val r=client.safe{val body=api.memberExport(format);kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO){body.use{response->application.contentResolver.openOutputStream(destination)?.use{output->response.byteStream().use{input->input.copyTo(output)}} ?: error("Unable to write the selected document.")}}}){
            is Result.Ok->{update{it.copy(busy=false)};onDone()};is Result.Err->fail("member_export",r)
        }
    }

    // ── Reports ───────────────────────────────────────────────────────────────

    fun loadReports() = viewModelScope.launch {
        update { it.copy(busy = true, errors = it.errors - "reports") }
        when (val r = client.safe { api.reports() }) {
            is Result.Ok  -> update { it.copy(busy = false, reports = r.value.results) }
            is Result.Err -> fail("reports", r)
        }
    }

    fun loadReport(id: Int) = viewModelScope.launch {
        when (val r = client.safe { api.report(id) }) {
            is Result.Ok  -> update { it.copy(selectedReport = r.value) }
            is Result.Err -> fail("report_detail", r)
        }
    }

    fun submitReport(title: String, content: String, period: String, deadline: String? = null, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        mutationUpdate { it.copy(busy = true, errors = it.errors - "submit_report") }
        when (val r = client.safe { api.createReport(ReportCreate(title, content, period, deadline?.ifBlank { null })) }) {
            is Result.Ok  -> {readFreshness.clear(); mutationUpdate { it.copy(busy = false) }; onResult(true);  loadReports() }
            is Result.Err -> { fail("submit_report", r);         onResult(false) }
        }
    }

    fun reviewReport(id: Int, action: String, notes: String, onResult: (Boolean) -> Unit = {}) = viewModelScope.launch {
        mutationUpdate { it.copy(busy = true) }
        when (val r = client.safe { api.reviewReport(id, ReportReview(action, notes)) }) {
            is Result.Ok  -> {readFreshness.clear(); mutationUpdate { it.copy(busy = false, selectedReport = r.value) }; onResult(true);  loadReports() }
            is Result.Err -> { fail("reports", r);                                          onResult(false) }
        }
    }

    fun escalateReport(id: Int, onResult: (Boolean) -> Unit = {}) = viewModelScope.launch {
        when (val r = client.safe { api.escalateReport(id) }) {
            is Result.Ok  -> {readFreshness.clear(); onResult(true);  loadReports() }
            is Result.Err -> { fail("reports", r); onResult(false) }
        }
    }

    // ── Events ────────────────────────────────────────────────────────────────

    fun loadEvents() = viewModelScope.launch {
        when (val r = client.safe { Page(results=allPages {api.events(it)}) }) {
            is Result.Ok  -> update { it.copy(events = r.value.results) }
            is Result.Err -> fail("events", r)
        }
    }

    fun loadEvent(id: Int) = viewModelScope.launch {
        when (val r = client.safe { api.event(id) }) {
            is Result.Ok  -> update { it.copy(selectedEvent = r.value) }
            is Result.Err -> fail("event_detail", r)
        }
    }

    fun createEvent(title: String, description: String, location: String, startDate: String, endDate: String, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        mutationUpdate { it.copy(busy = true, errors = it.errors - "create_event") }
        val body = EventItem(title = title, description = description, location = location, start_date = startDate, end_date = endDate)
        when (val r = client.safe { api.createEvent(body) }) {
            is Result.Ok  -> {readFreshness.clear(); mutationUpdate { it.copy(busy = false) }; onResult(true);  loadEvents() }
            is Result.Err -> { fail("create_event", r);          onResult(false) }
        }
    }

    fun saveAttendance(eventId: Int, records: List<AttendanceRecord>, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        mutationUpdate { it.copy(busy = true) }
        when (val r = client.safe { api.attendance(eventId, AttendanceRequest(records)) }) {
            is Result.Ok  -> {readFreshness.clear(); mutationUpdate { it.copy(busy = false) }; onResult(true) }
            is Result.Err -> { fail("attendance", r);            onResult(false) }
        }
    }

    // ── Ward meetings ─────────────────────────────────────────────────────────

    fun loadWardMeetings() = viewModelScope.launch {
        when (val r = client.safe { allPages { page -> api.wardMeetings(page) } }) {
            is Result.Ok  -> update { it.copy(wardMeetings = r.value) }
            is Result.Err -> fail("ward_meetings", r)
        }
    }

    fun createWardMeeting(title: String, date: String, time: String?, location: String, agenda: String, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        mutationUpdate { it.copy(busy = true, errors = it.errors - "ward_meeting") }
        val body = WardMeeting(title = title, date = date, time = time, location = location, agenda = agenda)
        when (val r = client.safe { api.createWardMeeting(body) }) {
            is Result.Ok  -> {readFreshness.clear(); mutationUpdate { it.copy(busy = false) }; onResult(true);  loadWardMeetings() }
            is Result.Err -> { fail("ward_meeting", r);          onResult(false) }
        }
    }

    // ── Notifications ─────────────────────────────────────────────────────────

    fun loadUnreadCount(force:Boolean=false)=cachedRead("notification_count",force=force,ttl=15_000) {
        when(val r=client.safe{api.notificationSummary()}){is Result.Ok->{update{it.copy(unreadCount=r.value.unread_count)};true};is Result.Err->{fail("notification_count",r);false}}
    }
    fun loadNotifications(more:Boolean=false,force:Boolean=false):Job {
        loadUnreadCount(force)
        val page=if(more)(publicPages["notifications"] ?: 1)+1 else 1
        return cachedRead("notifications","$page",force||more,ttl=15_000) {
            when(val r=client.safe{api.notifications(page)}){
                is Result.Ok->{publicPages["notifications"]=page;update{it.copy(notifications=if(more)it.notifications+r.value.results else r.value.results,hasMore=it.hasMore+("notifications" to (r.value.next!=null)))};true}
                is Result.Err->{fail("notifications",r);false}
            }
        }
    }
    fun readNotification(id:Int) = notificationState(id,true)
    fun notificationState(id:Int,read:Boolean) = viewModelScope.launch {
        val r=client.safe { if(read) api.readNotification(id) else api.notificationState(id,mapOf("is_read" to false)) }
        when(r) { is Result.Ok -> { update { it.copy(notifications=it.notifications.map { n -> if(n.id==id) n.copy(is_read=read) else n }) };loadNotifications(force=true) };is Result.Err -> fail("notifications",r) }
    }
    fun readAll() = viewModelScope.launch {
        when(val r=client.safe { api.readAll() }) { is Result.Ok -> loadNotifications(force=true); is Result.Err -> fail("notifications",r) }
    }

    // ── Programmes ────────────────────────────────────────────────────────────

    fun loadPrograms(kind: String) = latest("programs") {
        update{it.copy(busy=true,programs=emptyList(),errors=it.errors-"programs")}
        when (val r = client.safe { Page(results=allPages {api.programs(kind,it)}) }) {
            is Result.Ok  -> update { it.copy(busy=false,programs = r.value.results) }
            is Result.Err -> fail("programs", r)
        }
    }

    fun createProgram(kind: String, item: ProgramItem, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        mutationUpdate { it.copy(busy = true) }
        when (val r = client.safe { api.createProgram(kind, item) }) {
            is Result.Ok  -> {readFreshness.clear(); mutationUpdate { it.copy(busy = false) }; onResult(true);  loadPrograms(kind) }
            is Result.Err -> { fail("programs", r);              onResult(false) }
        }
    }

    // ── Finance ───────────────────────────────────────────────────────────────

    fun loadFinance() = viewModelScope.launch {
        when (val r = client.safe { api.finance() }) {
            is Result.Ok  -> update { it.copy(finance = r.value) }
            is Result.Err -> fail("finance", r)
        }
    }

    fun loadDonations() = viewModelScope.launch {
        when (val r = client.safe { allPages { page -> api.donations(page) } }) {
            is Result.Ok  -> update { it.copy(donations = r.value) }
            is Result.Err -> fail("finance", r)
        }
    }

    fun createDonation(donor: String, amount: String, reference: String, notes: String, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        mutationUpdate { it.copy(busy = true) }
        val body = Donation(donor_name = donor, amount = amount, reference = reference, notes = notes)
        when (val r = client.safe { api.createDonation(body) }) {
            is Result.Ok  -> {readFreshness.clear(); mutationUpdate { it.copy(busy = false) }; onResult(true);  loadDonations() }
            is Result.Err -> { fail("finance", r);               onResult(false) }
        }
    }

    // ── Discipline ────────────────────────────────────────────────────────────

    fun loadDiscipline() = viewModelScope.launch {
        when (val r = client.safe { api.discipline() }) {
            is Result.Ok  -> update { it.copy(disciplines = r.value.results) }
            is Result.Err -> fail("discipline", r)
        }
    }

    fun createDiscipline(userId:Int,type:String,reason:String,onDone:()->Unit) = viewModelScope.launch {
        mutationUpdate { it.copy(busy=true,errors=it.errors-"discipline") }
        when(val r=client.safe{api.createDiscipline(Discipline(user=userId,action_type=type,reason=reason))}) {
            is Result.Ok->{readFreshness.clear();mutationUpdate{it.copy(busy=false)};onDone();loadDiscipline()}
            is Result.Err->fail("discipline",r)
        }
    }
    fun decideDisciplineAction(id: Int, decision: String, onDone:()->Unit={}) = viewModelScope.launch {
        mutationUpdate { it.copy(busy=true,errors=it.errors-"discipline") }
        when (val r = client.safe { api.decideDiscipline(id, DisciplineDecision(decision)) }) {
            is Result.Ok  -> {readFreshness.clear(); mutationUpdate{it.copy(busy=false)};onDone();loadDiscipline() }
            is Result.Err -> fail("discipline", r)
        }
    }

    // ── Media ─────────────────────────────────────────────────────────────────

    fun loadMedia(review: Boolean = false) = viewModelScope.launch {
        when (val r = client.safe { if (review) api.mediaQueue() else api.myMedia() }) {
            is Result.Ok  -> update { it.copy(media = r.value.results) }
            is Result.Err -> fail("media", r)
        }
    }

    fun decideMedia(id: Int, action: String) = viewModelScope.launch {
        when (val r = client.safe { api.decideMedia(id, mapOf("action" to action)) }) {
            is Result.Ok  -> loadMedia(true)
            is Result.Err -> fail("media", r)
        }
    }

    // ── Editorial ─────────────────────────────────────────────────────────────

    fun loadEditorialQueue() = viewModelScope.launch {
        update { it.copy(busy = true, errors = it.errors - "editorial") }
        when (val r = client.safe { Page(results=allPages {api.articleQueue(it)}) }) {
            is Result.Ok  -> update { it.copy(busy = false, editorialQueue = r.value.results) }
            is Result.Err -> fail("editorial", r)
        }
    }

    fun loadMyArticles() = viewModelScope.launch {
        update { it.copy(busy = true) }
        when (val r = client.safe { Page(results=allPages {api.myArticles(it)}) }) {
            is Result.Ok  -> update { it.copy(busy = false, articles = r.value.results) }
            is Result.Err -> fail("articles", r)
        }
    }

    fun loadEditorLocations()=latest("editor_locations") {
        when(val r=client.safe{allPages{api.lgas(null,it)}}){is Result.Ok->update{it.copy(lgas=r.value)};is Result.Err->fail("locations",r)}
    }
    fun loadDraftForEditing(id:Int)=viewModelScope.launch {
        update{it.copy(busy=true,selectedArticle=null,errors=it.errors-"article_compose")}
        when(val r=client.safe{api.myArticle(id)}){is Result.Ok->update{it.copy(busy=false,selectedArticle=r.value)};is Result.Err->fail("article_compose",r)}
    }
    fun uploadArticleBodyImage(uri:Uri,onResult:(String?)->Unit)=viewModelScope.launch {
        update{it.copy(busy=true,errors=it.errors-"article_image")}
        when(val r=client.safe{api.uploadArticleImage(uploadPart(application,uri,"image",maxBytes=2*1024*1024))}){is Result.Ok->{update{it.copy(busy=false)};onResult(r.value["url"])};is Result.Err->{fail("article_image",r);onResult(null)}}
    }
    fun saveArticleEditor(id:Int?,body:Map<String,Any?>,photo:Uri?,onResult:(Article?)->Unit)=viewModelScope.launch {
        mutationUpdate{it.copy(busy=true,errors=it.errors-"article_compose")}
        when(val result=client.safe{
            if(photo==null){if(id==null)api.createArticle(body) else api.editArticle(id,body)}
            else {
                val fields=body.mapValues{(name,value)->val text=if(name=="content_json")client.moshi.adapter(Any::class.java).toJson(value) else if(value==null)"" else if(value is Number&&value.toDouble()%1.0==0.0)value.toInt().toString() else value.toString();text.toRequestBody("text/plain".toMediaType())}
                val files=listOf(uploadPart(application,photo,"featured_image"))
                if(id==null)api.createArticleMultipart(fields,files) else api.editArticleMultipart(id,fields,files)
            }
        }){is Result.Ok->{readFreshness.clear();mutationUpdate{it.copy(busy=false)};onResult(result.value)};is Result.Err->{fail("article_compose",result);onResult(null)}}
    }
    fun createArticleDraft(headline: String, subheadline: String, category: String, content: String, lga: Int? = null, ward: Int? = null, onResult: (Article?) -> Unit) = viewModelScope.launch {
        mutationUpdate { it.copy(busy = true, errors = it.errors - "article_compose") }
        val body = buildMap<String, @JvmSuppressWildcards Any?> {
            put("title", headline); put("subheadline", subheadline); put("category", category); put("content", content)
            lga?.let { put("lga", it) }; ward?.let { put("ward", it) }
        }
        when (val r = client.safe { api.createArticle(body) }) {
            is Result.Ok  -> {readFreshness.clear(); mutationUpdate { it.copy(busy = false) }; onResult(r.value) }
            is Result.Err -> { fail("article_compose", r);       onResult(null) }
        }
    }

    fun submitArticleForReview(id: Int, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        mutationUpdate { it.copy(busy = true) }
        when (val r = client.safe { api.submitArticle(id) }) {
            is Result.Ok  -> {readFreshness.clear(); mutationUpdate { it.copy(busy = false) }; onResult(true) }
            is Result.Err -> { fail("article_compose", r);       onResult(false) }
        }
    }

    fun decideArticle(id: Int, action: String, note:String="", onResult: (Boolean) -> Unit = {}) = viewModelScope.launch {
        when (val r = client.safe { api.decideArticle(id, mapOf("action" to action,"note" to note)) }) {
            is Result.Ok  -> {readFreshness.clear(); loadEditorialQueue(); onResult(true) }
            is Result.Err -> { fail("editorial", r); onResult(false) }
        }
    }

    // ── Profile ───────────────────────────────────────────────────────────────

    fun updateProfile(p: ProfileUpdate) = viewModelScope.launch {
        mutationUpdate { it.copy(busy = true, errors = it.errors - "profile") }
        when (val r = client.safe { api.updateProfile(p) }) {
            is Result.Ok  -> when (val me = client.safe { api.me() }) {
                is Result.Ok  -> resolveUser(me.value)
                is Result.Err -> fail("profile", me)
            }
            is Result.Err -> fail("profile", r)
        }
    }

    // ── Telegram ──────────────────────────────────────────────────────────────

    fun createTelegramLink(onReady: (String) -> Unit) = viewModelScope.launch {
        when (val r = client.safe { api.telegramLink() }) {
            is Result.Ok  -> {readFreshness.clear(); mutationUpdate { it.copy(telegramLink = r.value.url) }; onReady(r.value.url) }
            is Result.Err -> fail("telegram", r)
        }
    }

    // ── Device registration ───────────────────────────────────────────────────

    // ── Missing Functionality Additions ──────────────────────────────────────────

    fun loadAnnouncements(mine: Boolean = false) = viewModelScope.launch {
        update { it.copy(busy = true, errors = it.errors - "announcements") }
        when (val r = client.safe { Page(results=allPages{api.announcements(mine=mine,page=it)}) }) {
            is Result.Ok -> update { it.copy(busy = false, announcements = r.value.results) }
            is Result.Err -> fail("announcements", r)
        }
    }
    
    fun loadReceivedAnnouncements() = viewModelScope.launch {
        update { it.copy(busy = true, errors = it.errors - "receivedAnnouncements") }
        when (val r = client.safe { Page(results=allPages{api.receivedAnnouncements(it)}) }) {
            is Result.Ok -> update { it.copy(busy = false, receivedAnnouncements = r.value.results) }
            is Result.Err -> fail("receivedAnnouncements", r)
        }
    }

    fun loadCommunityReports() = viewModelScope.launch {
        update { it.copy(busy = true, errors = it.errors - "communityReports") }
        when (val r = client.safe { allPages { page -> api.communityReports(page) } }) {
            is Result.Ok -> update { it.copy(busy = false, communityReports = r.value) }
            is Result.Err -> fail("communityReports", r)
        }
    }
    
    fun submitCommunityReport(report: CommunityReport, image:Uri?=null, video:Uri?=null, onDone: () -> Unit = {}) = viewModelScope.launch {
        mutationUpdate { it.copy(busy = true, errors = it.errors - "submitCommunityReport") }
        when (val r = client.safe {
            val fields=mapOf("location_details" to report.location_details,"incident_date" to report.incident_date,"category" to report.category,"what_happened" to report.what_happened,"who_was_involved" to report.who_was_involved,"why_is_it_important" to report.why_is_it_important).toMutableMap()
            if(!report.incident_time.isNullOrBlank())fields["incident_time"]=report.incident_time
            val parts=buildList {image?.let{add(uploadPart(getApplication(),it,"evidence_image"))};video?.let{add(uploadPart(getApplication(),it,"evidence_video"))}}
            api.createCommunityReportMultipart(fields.mapValues{it.value.toRequestBody("text/plain".toMediaType())},parts)
        }) {
            is Result.Ok -> {readFreshness.clear(); mutationUpdate { it.copy(busy = false, communityReports = listOf(r.value) + it.communityReports) }; onDone() }
            is Result.Err -> fail("submitCommunityReport", r)
        }
    }
    
    fun reviewCommunityReport(id: Int, decision: CommunityReportDecision, onDone: () -> Unit = {}) = viewModelScope.launch {
        mutationUpdate { it.copy(busy = true, errors = it.errors - "reviewCommunityReport") }
        when (val r = client.safe { api.reviewCommunityReport(id, decision) }) {
            is Result.Ok -> {readFreshness.clear(); mutationUpdate { it.copy(busy = false, communityReports = it.communityReports.map { if (it.id == id) r.value else it }) }; onDone() }
            is Result.Err -> fail("reviewCommunityReport", r)
        }
    }

    fun loadReporters() = viewModelScope.launch {
        update { it.copy(busy = true, errors = it.errors - "reporters") }
        when (val r = client.safe { allPages { page -> api.reporters(page) } }) {
            is Result.Ok -> update { it.copy(busy = false, reporters = r.value) }
            is Result.Err -> fail("reporters", r)
        }
    }
    
    fun reporterAction(id: Int, action: String) = viewModelScope.launch {
        update { it.copy(busy = true, errors = it.errors - "reporterAction") }
        when (val r = client.safe { api.reporterAction(id, action) }) {
            is Result.Ok -> update { it.copy(busy = false, reporters = it.reporters.map { if (it.id == id) r.value else it }) }
            is Result.Err -> fail("reporterAction", r)
        }
    }

    fun verifyDonation(id: Int) = viewModelScope.launch {
        mutationUpdate { it.copy(busy = true, errors = it.errors - "verifyDonation") }
        when (val r = client.safe { api.verifyDonation(id) }) {
            is Result.Ok -> mutationUpdate { it.copy(busy = false, donations = it.donations.map { if (it.id == id) r.value else it }) }
            is Result.Err -> fail("verifyDonation", r)
        }
    }

    fun recordDonation(id: Int) = viewModelScope.launch {
        mutationUpdate { it.copy(busy = true, errors = it.errors - "recordDonation") }
        when (val r = client.safe { api.recordDonation(id) }) {
            is Result.Ok -> mutationUpdate { it.copy(busy = false, donations = it.donations.map { if (it.id == id) r.value else it }) }
            is Result.Err -> fail("recordDonation", r)
        }
    }

    fun loadFinancialReports() = viewModelScope.launch {
        update { it.copy(busy = true, errors = it.errors - "financialReports") }
        when (val r = client.safe { allPages { page -> api.financialReports(page) } }) {
            is Result.Ok -> update { it.copy(busy = false, financialReports = r.value) }
            is Result.Err -> fail("financialReports", r)
        }
    }

    fun loadAuditReports() = viewModelScope.launch {
        update { it.copy(busy = true, errors = it.errors - "auditReports") }
        when (val r = client.safe { allPages { page -> api.auditReports(page) } }) {
            is Result.Ok -> update { it.copy(busy = false, auditReports = r.value) }
            is Result.Err -> fail("auditReports", r)
        }
    }

    fun submitAudit(id:Int)=viewModelScope.launch {
        mutationUpdate{it.copy(busy=true,errors=it.errors-"auditReports")}
        when(val r=client.safe{api.submitAuditReport(id)}){is Result.Ok->{readFreshness.clear();mutationUpdate{it.copy(busy=false)};loadAuditReports()};is Result.Err->fail("auditReports",r)}
    }
    fun submitLegalOpinion(id: Int, request: LegalOpinionRequest, onDone: () -> Unit = {}) = viewModelScope.launch {
        mutationUpdate { it.copy(busy = true, errors = it.errors - "legalOpinion") }
        when (val r = client.safe { api.submitLegalOpinion(id, request) }) {
            is Result.Ok -> {readFreshness.clear(); mutationUpdate { it.copy(busy = false, disciplines = it.disciplines.map { if (it.id == id) r.value else it }) }; onDone() }
            is Result.Err -> fail("legalOpinion", r)
        }
    }
    
    fun changePassword(old: String, new: String, onDone: () -> Unit = {}) = viewModelScope.launch {
        update { it.copy(busy = true, errors = it.errors - "changePassword") }
        when (val r = client.safe { api.changePassword(PasswordChange(old, new)) }) {
            is Result.Ok -> { update { it.copy(busy = false) }; onDone() }
            is Result.Err -> fail("changePassword", r)
        }
    }
    
    fun loadOutreach() = viewModelScope.launch {
        update { it.copy(busy = true, errors = it.errors - "outreach") }
        when (val r = client.safe { allPages { page -> api.outreachList(page) } }) {
            is Result.Ok -> update { it.copy(busy = false, outreach = r.value) }
            is Result.Err -> fail("outreach", r)
        }
    }

    fun submitOutreach(outreach: CommunityOutreach, onDone: () -> Unit = {}) = viewModelScope.launch {
        mutationUpdate { it.copy(busy = true, errors = it.errors - "outreach") }
        when (val r = client.safe { api.createOutreach(outreach) }) {
            is Result.Ok -> {readFreshness.clear();
                mutationUpdate { it.copy(busy = false, outreach = listOf(r.value) + it.outreach) }
                onDone()
            }
            is Result.Err -> fail("outreach", r)
        }
    }

    fun saveProfile(values:Map<String,String>,photo:Uri?,onDone:()->Unit) = viewModelScope.launch {
        mutationUpdate {it.copy(busy=true,errors=it.errors-"profile")}
        when(val r=client.safe {if(photo==null)api.updateProfile(values) else api.updateProfilePhoto(values.mapValues{it.value.toRequestBody("text/plain".toMediaType())},uploadPart(application,photo,"photo",true))}) {
            is Result.Ok -> {readFreshness.clear();when(val me=client.safe{api.me()}){is Result.Ok -> {readFreshness.clear();resolveUser(me.value);onDone()};is Result.Err -> fail("profile",me)}}
            is Result.Err -> fail("profile",r)
        }
    }

    fun loadResourceCatalog() = viewModelScope.launch {
        when(val r=client.safe {api.resourceCatalog()}){is Result.Ok -> update {it.copy(resourceCatalog=r.value.resources)};is Result.Err -> fail("resources",r)}
    }
    fun loadResources(kind:String) = viewModelScope.launch {
        update {it.copy(busy=true,resourceRows=emptyList(),errors=it.errors-"resources")}
        when(val r=client.safe {allPages {api.resources(kind,it)}}){is Result.Ok -> update {it.copy(busy=false,resourceRows=r.value)};is Result.Err -> fail("resources",r)}
    }
    fun saveResource(kind:String,id:Int?,values:Map<String,String>,files:Map<String,Uri>,onDone:()->Unit) = viewModelScope.launch {
        mutationUpdate {it.copy(busy=true,errors=it.errors-"resources")}
        when(val r=client.safe {
            val fields=(if(id==null)values.filterValues {it.isNotBlank()} else values).mapValues{it.value.toRequestBody("text/plain".toMediaType())}
            val parts=files.map{(key,uri)->uploadPart(application,uri,key)}
            if(id==null)api.createResource(kind,fields,parts) else api.editResource(kind,id,fields,parts)
        }){is Result.Ok -> {readFreshness.clear();mutationUpdate {it.copy(busy=false)};onDone()};is Result.Err -> fail("resources",r)}
    }
    fun deleteResource(kind:String,id:Int,onDone:()->Unit) = viewModelScope.launch {
        when(val r=client.safe {api.deleteResource(kind,id).also {check(it.isSuccessful){"Delete failed (${it.code()})."}}}){is Result.Ok -> {readFreshness.clear();loadResources(kind);onDone()};is Result.Err -> fail("resources",r)}
    }
    fun selectEvent(event:EventItem)=update {it.copy(selectedEvent=event)}
    fun loadMinutes(id:Int) = viewModelScope.launch {
        update {it.copy(minutes=null,errors=it.errors-"minutes")}
        when(val r=client.safe {api.minutes(id)}){is Result.Ok -> update {it.copy(minutes=r.value)};is Result.Err -> if(r.status!=404)fail("minutes",r)}
    }
    fun saveMinutes(id:Int,body:MeetingMinutes,onDone:()->Unit) = viewModelScope.launch {
        mutationUpdate {it.copy(busy=true,errors=it.errors-"minutes")}
        when(val r=client.safe {api.saveMinutes(id,body)}){is Result.Ok -> {readFreshness.clear();mutationUpdate {it.copy(busy=false,minutes=r.value)};onDone()};is Result.Err -> fail("minutes",r)}
    }
    fun loadWardAttendance(id:Int)=viewModelScope.launch {
        update{it.copy(attendance=emptyList(),errors=it.errors-"attendance")}
        when(val r=client.safe{api.wardAttendance(id)}){is Result.Ok->update{it.copy(attendance=r.value)};is Result.Err->fail("attendance",r)}
    }
    fun saveWardAttendance(id:Int,records:List<AttendanceRecord>,onDone:()->Unit)=viewModelScope.launch {
        mutationUpdate{it.copy(busy=true)}
        when(val r=client.safe{api.saveWardAttendance(id,AttendanceRequest(records))}){is Result.Ok->{readFreshness.clear();mutationUpdate{it.copy(busy=false,attendance=r.value)};onDone()};is Result.Err->fail("attendance",r)}
    }
    fun loadAttendance(id:Int) = viewModelScope.launch {
        update{it.copy(attendance=emptyList(),errors=it.errors-"attendance")}
        when(val r=client.safe {api.eventAttendance(id)}){is Result.Ok -> update {it.copy(attendance=r.value)};is Result.Err -> fail("attendance",r)}
    }
    fun updateAnnouncement(body:AnnouncementDto,onDone:()->Unit={})=viewModelScope.launch {
        mutationUpdate{it.copy(busy=true,errors=it.errors-"announcements")}
        when(val r=client.safe{api.updateAnnouncement(body.id,body)}){is Result.Ok->{readFreshness.clear();mutationUpdate{it.copy(busy=false)};loadAnnouncements(true);onDone()};is Result.Err->fail("announcements",r)}
    }
    fun deleteAnnouncement(id:Int,onDone:()->Unit)=viewModelScope.launch {
        mutationUpdate{it.copy(busy=true,errors=it.errors-"announcements")}
        when(val r=client.safe{api.deleteAnnouncement(id)}){is Result.Ok->{readFreshness.clear();mutationUpdate{it.copy(busy=false)};loadAnnouncements(true);onDone()};is Result.Err->fail("announcements",r)}
    }
    fun createAnnouncement(body:AnnouncementDto,onDone:()->Unit)=viewModelScope.launch {
        mutationUpdate {it.copy(busy=true,errors=it.errors-"announcements")}
        when(val r=client.safe{api.createAnnouncement(body)}){is Result.Ok->{readFreshness.clear();mutationUpdate{it.copy(busy=false)};loadAnnouncements(true);onDone()};is Result.Err->fail("announcements",r)}
    }
    fun loadProgramParticipants(kind:String,id:Int) = viewModelScope.launch {
        update{it.copy(participantIds=emptyList(),errors=it.errors-"participants")}
        when(val r=client.safe {api.programParticipants(kind,id)}){is Result.Ok -> update {it.copy(participantIds=r.value.members)};is Result.Err -> fail("participants",r)}
    }
    fun saveProgramParticipants(kind:String,id:Int,members:List<Int>,onDone:()->Unit) = viewModelScope.launch {
        mutationUpdate {it.copy(busy=true)}
        when(val r=client.safe {api.saveParticipants(kind,id,ParticipantSet(members))}){is Result.Ok -> {readFreshness.clear();mutationUpdate {it.copy(busy=false,participantIds=r.value.members)};onDone()};is Result.Err -> fail("participants",r)}
    }
    fun uploadMedia(uri:Uri,title:String,description:String,type:String,onDone:()->Unit) = viewModelScope.launch {
        update {it.copy(busy=true,errors=it.errors-"media_upload")}
        when(val r=client.safe {api.uploadMedia(uploadPart(application,uri,"file"),title.toRequestBody("text/plain".toMediaType()),description.toRequestBody("text/plain".toMediaType()),type.toRequestBody("text/plain".toMediaType()))}){is Result.Ok -> {update {it.copy(busy=false)};onDone()};is Result.Err -> fail("media_upload",r)}
    }
    fun editDraft(id:Int,body:Map<String,Any?>,onDone:()->Unit) = viewModelScope.launch {
        when(val r=client.safe {api.editArticle(id,body)}){is Result.Ok -> {loadMyArticles();onDone()};is Result.Err -> fail("articles",r)}
    }
    fun deleteDraft(id:Int,onDone:()->Unit) = viewModelScope.launch {
        when(val r=client.safe {api.deleteArticle(id).also {check(it.isSuccessful){"Delete failed (${it.code()})."}}}){is Result.Ok -> {readFreshness.clear();loadMyArticles();onDone()};is Result.Err -> fail("articles",r)}
    }

    private fun registerCurrentDevice() {
        val userId=_state.value.user?.id ?: return
        runCatching { FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            if(signingOut || _state.value.user?.id!=userId || store.access()==null) return@addOnSuccessListener
            val id = store.installationId()
            store.saveDevice(token, id)
            deviceRegistrationJob?.cancel()
            deviceRegistrationJob=viewModelScope.launch {
                val result=client.safe { api.registerDevice(DeviceRegistration(token = token, device_id = id, app_version = com.kpn.mobile.BuildConfig.VERSION_NAME)) }
                if(result is Result.Ok && _state.value.user?.id==userId) deviceRegistrationUser=userId
            }
        }.addOnFailureListener { /* FCM unavailable: keep the valid API session. */ } }
    }
}
