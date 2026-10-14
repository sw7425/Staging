@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.kpn.mobile.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kpn.mobile.R
import com.kpn.mobile.core.ContentChoices
import com.kpn.mobile.core.KpnDates
import com.kpn.mobile.core.AppState
import com.kpn.mobile.core.AppViewModel
import androidx.navigation.NavHostController
import com.kpn.mobile.data.*

private fun publicRoute(s:AppState,route:String)=com.kpn.mobile.core.SignedInNavigation.publicRoute(s.user!=null,route)
@Composable internal fun HomeScreen(vm:AppViewModel,s:AppState,nav:NavHostController) {
    val homeCopy = rememberCanonicalCopy("home")
    LaunchedEffect(Unit){vm.loadHome();vm.loadPublicSection("advocacy")}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(bottom=24.dp)) {
        item{KpnTopBar(if(s.user!=null)"Home" else "KPN network",actions={IconButton(onClick={vm.loadHome(force=true)}){Icon(Icons.Default.Refresh,"Refresh Home")}})}
        item{Loading("home" in s.loading);ErrorState(s.errors["home"]){vm.loadHome(force=true)}}
        s.homeContent?.let { home ->
            item { HomeIntroduction(home,s.user!=null,{nav.navigate("register")},{nav.navigate(publicRoute(s,"news"))}) }
            item { Column(Modifier.padding(horizontal=20.dp,vertical=20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement=Arrangement.spacedBy(4.dp)) { Text("The KPN Ecosystem",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary);Text("Our 5 Core Pillars",style=MaterialTheme.typography.headlineSmall) }
                HomePillars(home.pillars){nav.navigate(publicRoute(s,it))}
            } }
        }
        if(s.advocacy.isNotEmpty()) {
            item { Column(Modifier.padding(horizontal=20.dp)) { SectionHeading("Featured Campaigns") } }
            items(s.advocacy.take(3),key={"campaign_${it.id}"}) { campaign ->
                OutlinedCard(onClick={nav.navigate("civic")},modifier=Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=6.dp),shape=MaterialTheme.shapes.medium,border=androidx.compose.foundation.BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.padding(16.dp),horizontalArrangement=Arrangement.spacedBy(14.dp),verticalAlignment=Alignment.CenterVertically) {
                        if(!campaign.image.isNullOrBlank())KpnImage(campaign.image,campaign.title,Modifier.size(64.dp))
                        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {Text(campaign.title,style=MaterialTheme.typography.titleMedium);Text(campaign.issue,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=3,overflow=TextOverflow.Ellipsis)}
                        Icon(Icons.Default.ChevronRight,null,Modifier.size(20.dp))
                    }
                }
            }
        }
        item { Loading("advocacy" in s.loading);ErrorState(s.errors["advocacy"]){vm.loadPublicSection("advocacy")} }
        item{Column(Modifier.padding(horizontal=20.dp)){Loading("home_news" in s.loading);SectionHeading("Latest News",action="View All",onAction={nav.navigate(publicRoute(s,"news"))});ErrorState(s.errors["home_news"]){vm.loadHome(force=true)}}}
        if(s.homeArticles.isEmpty()&&!("home_news" in s.loading)&&s.errors["home_news"]==null) item{EmptyState("No published stories yet.")}
        items(s.homeArticles.take(3),key={it.id}){article->EditorialRow(article,article==s.homeArticles.firstOrNull()){nav.navigate("article/${article.slug}")}}
        item{Column(Modifier.padding(horizontal=20.dp)){Loading("home_opportunities" in s.loading);SectionHeading("Current opportunities",action="View all",onAction={nav.navigate(publicRoute(s,"opportunities"))});ErrorState(s.errors["home_opportunities"]){vm.loadHome(force=true)}}}
        if(s.homeOpportunities.isEmpty()&&!("home_opportunities" in s.loading)&&s.errors["home_opportunities"]==null)item{EmptyState("No current opportunities have been published.")}
        items(s.homeOpportunities.take(2),key={"opportunity_${it.id}"}){o->OpportunityItem(o)}
        if(s.user==null) homeCopy?.let { copy -> item { Surface(color=MaterialTheme.colorScheme.primaryContainer) { Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) { copy.blocks.takeLast(3).take(2).forEach { CanonicalBlock(it) };KpnPrimaryButton(copy.blocks.last().text,false){nav.navigate("register")} } } } }
    }
}
@Composable private fun EditorialRow(a:Article,featured:Boolean=false,onClick:()->Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick=onClick).padding(horizontal=20.dp,vertical=12.dp)) {
        if(featured) KpnImage(a.image,a.title,Modifier.fillMaxWidth().aspectRatio(16f/9f))
        else if(!a.image.isNullOrBlank()) KpnImage(a.image,a.title,Modifier.fillMaxWidth().height(120.dp))
        Row(Modifier.padding(top=if(featured)12.dp else 0.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){Text(ContentChoices.label(ContentChoices.news,a.category),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary);a.published_at?.let{Text(KpnDates.date(it),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
        Text(a.title,style=if(featured)MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,modifier=Modifier.padding(vertical=6.dp))
        if(a.subheadline.isNotBlank())Text(a.subheadline,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=3,overflow=TextOverflow.Ellipsis)
        Row(Modifier.padding(top=10.dp),verticalAlignment=Alignment.CenterVertically){StatusBadge(a.verification_status);Spacer(Modifier.weight(1f));Text("${a.read_time} min read",style=MaterialTheme.typography.labelSmall)}
        Spacer(Modifier.height(14.dp));KpnDivider()
    }
}
@Composable internal fun NewsroomScreen(vm:AppViewModel,s:AppState,nav:NavHostController) {
    var query by remember {mutableStateOf("")};var category by remember {mutableStateOf<String?>(null)}
    LaunchedEffect(query,category){kotlinx.coroutines.delay(350);vm.loadNews(query,category)}
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Newsroom",actions={IconButton(onClick={vm.loadNews(query,category,force=true)}){Icon(Icons.Default.Refresh,"Refresh news")}})
        OutlinedTextField(value=query,onValueChange={query=it},placeholder={Text("Search published news")},singleLine=true,shape=MaterialTheme.shapes.small,leadingIcon={Icon(Icons.Default.Search,null)},trailingIcon={if(query.isNotEmpty())IconButton(onClick={query=""}){Icon(Icons.Default.Close,"Clear search")}},modifier=Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=8.dp))
        LazyRow(contentPadding=PaddingValues(horizontal=20.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){items((listOf("All")+com.kpn.mobile.core.ContentChoices.news.map{it.first})){c->FilterChip(selected=category==c||c=="All"&&category==null,onClick={category=if(c=="All")null else c},label={Text(c.lowercase().replaceFirstChar{it.uppercase()})})}}
        Spacer(Modifier.height(8.dp));KpnDivider();Loading(("news" in s.loading));ErrorState(s.errors["news"]){vm.loadNews(query,category,force=true)}
        LazyColumn {
            if(s.articles.isEmpty()&&!("news" in s.loading)&&s.errors["news"]==null)item{EmptyState("No stories match your search.")}
            items(s.articles,key={it.id}){a->EditorialRow(a,a==s.articles.firstOrNull()){nav.navigate("article/${a.slug}")}}
            if(s.hasMore["news"]==true)item{TextButton(onClick={vm.loadNews(query,category,true)},enabled=!("news" in s.loading),modifier=Modifier.fillMaxWidth()){Text("Load more stories")}}
        }
    }
}
@Composable internal fun ArticleDetailScreen(vm:AppViewModel,s:AppState,slug:String,nav:NavHostController) {
    LaunchedEffect(slug){vm.loadArticle(slug)}
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Newsroom",back={nav.popBackStack()})
        Loading(s.busy);ErrorState(s.errors["article"]){vm.loadArticle(slug)}
        s.selectedArticle?.let{a->LazyColumn(contentPadding=PaddingValues(20.dp)) {
            item {Text(ContentChoices.label(ContentChoices.news,a.category),style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.primary);Text(a.title,style=MaterialTheme.typography.headlineMedium,modifier=Modifier.padding(vertical=12.dp));if(a.subheadline.isNotBlank())Text(a.subheadline,style=MaterialTheme.typography.bodyLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)}
            item{Row(Modifier.padding(vertical=16.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){StatusBadge(a.verification_status);Text(KpnDates.date(a.published_at),style=MaterialTheme.typography.labelSmall)}}
            if(!a.image.isNullOrBlank())item{KpnImage(a.image,a.title,Modifier.fillMaxWidth().aspectRatio(16f/9f));Spacer(Modifier.height(20.dp))}
            item{Text(a.author_name ?: a.reporter_credit.replace('_',' '),style=MaterialTheme.typography.labelMedium);Spacer(Modifier.height(12.dp));ArticleBody(a)}
            if(a.verification_status=="DEVELOPING"||a.verification_status=="COMMUNITY_ALERT")item{Spacer(Modifier.height(20.dp));InfoBanner("Information status: ${a.verification_status.replace('_',' ')}. Follow the newsroom for updates.",isWarning=true)}
        }}
    }
}
@Composable private fun OpportunityItem(o:Opportunity) {
    var expanded by remember {mutableStateOf(false)};val context=LocalContext.current
    if(expanded) ModalBottomSheet(sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),onDismissRequest={expanded=false}) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
            StatusBadge(o.status);Text(o.title,style=MaterialTheme.typography.headlineSmall,modifier=Modifier.padding(vertical=12.dp));Text(o.organization,style=MaterialTheme.typography.titleSmall)
            o.deadline?.let{Text("Deadline: ${KpnDates.date(it)}",style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(vertical=12.dp))}
            SectionHeading("Overview");ReadableBody(o.description)
            if(o.requirements.isNotBlank()){SectionHeading("Requirements");ReadableBody(o.requirements)}
            if(o.benefits.isNotBlank()){SectionHeading("Benefits");ReadableBody(o.benefits)}
            if(o.external_apply_link.isNotBlank())KpnPrimaryButton("Open official application",false){val uri=Uri.parse(o.external_apply_link);if(uri.scheme in listOf("https","http"))runCatching{context.startActivity(Intent(Intent.ACTION_VIEW,uri))}}
            Spacer(Modifier.height(24.dp))
        }
    }
    OutlinedCard(onClick={expanded=true},modifier=Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=6.dp),shape=MaterialTheme.shapes.medium,border=androidx.compose.foundation.BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically){Text(ContentChoices.label(ContentChoices.opportunities,o.category),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary);Spacer(Modifier.weight(1f));StatusBadge(o.status)}
            Text(o.title,style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(top=10.dp,bottom=4.dp));Text(o.organization,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            o.deadline?.let{Text("Deadline · ${KpnDates.date(it)}",style=MaterialTheme.typography.labelSmall,modifier=Modifier.padding(top=10.dp))}
            Text("View details",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.secondary,modifier=Modifier.padding(top=12.dp))
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun OpportunityScreen(vm:AppViewModel,s:AppState,nav:NavHostController?=null) {
    var category by remember{mutableStateOf<String?>(null)};LaunchedEffect(category){vm.loadOpportunities(category)}
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Opportunities",actions={IconButton(onClick={vm.loadOpportunities(category,force=true)}){Icon(Icons.Default.Refresh,"Refresh opportunities")}})
        LazyRow(contentPadding=PaddingValues(horizontal=20.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){items((listOf("All")+com.kpn.mobile.core.ContentChoices.opportunities.map{it.first})){c->FilterChip(selected=category==c||c=="All"&&category==null,onClick={category=if(c=="All")null else c},label={Text(c.lowercase().replaceFirstChar{it.uppercase()})})}}
        Loading(("opportunities" in s.loading));ErrorState(s.errors["opportunities"]){vm.loadOpportunities(category,force=true)}
        LazyColumn(contentPadding=PaddingValues(vertical=12.dp)) {
            if(s.opportunities.isEmpty()&&!("opportunities" in s.loading)&&s.errors["opportunities"]==null)item{EmptyState("No active opportunities for this category.")}
            items(s.opportunities,key={it.id}){OpportunityItem(it)}
            if(s.hasMore["opportunities"]==true)item{TextButton(onClick={vm.loadOpportunities(category,true)},enabled=!("opportunities" in s.loading),modifier=Modifier.fillMaxWidth()){Text("Load more opportunities")}}
        }
    }
}
@Composable internal fun CommunityScreen(vm:AppViewModel,s:AppState,nav:NavHostController) {
    LaunchedEffect(Unit){vm.loadPublicSection("community")};var selected by remember{mutableStateOf<Initiative?>(null)}
    selected?.let{i->ModalBottomSheet(sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),onDismissRequest={selected=null}){Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)){Text(i.title,style=MaterialTheme.typography.headlineSmall);StatusBadge(i.status);Spacer(Modifier.height(16.dp));if(i.image!=null)KpnImage(i.image,i.title,Modifier.fillMaxWidth().aspectRatio(16f/9f));ReadableBody(i.description);if(i.location_text.isNotBlank())Text(i.location_text,style=MaterialTheme.typography.bodySmall);Text("People reached: ${i.people_reached}",style=MaterialTheme.typography.bodySmall);Spacer(Modifier.height(24.dp))}}}
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Community",back={nav.popBackStack()});Loading(("community" in s.loading));ErrorState(s.errors["community"]){vm.loadPublicSection("community")}
        LazyColumn(contentPadding=PaddingValues(20.dp)) {
            item{SectionHeading("Community initiatives")}
            if(s.initiatives.isEmpty()&&!("community" in s.loading)&&s.errors["community"]==null)item{EmptyState("No initiatives have been published yet.")}
            items(s.initiatives,key={it.id}){i->Column(Modifier.clickable{selected=i}.padding(vertical=12.dp)){if(i.image!=null)KpnImage(i.image,i.title,Modifier.fillMaxWidth().aspectRatio(16f/9f));Text(i.title,style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(vertical=8.dp));Text(i.location_text,style=MaterialTheme.typography.bodySmall);StatusBadge(i.status);Spacer(Modifier.height(12.dp));KpnDivider()}}
            if(s.hasMore["community"]==true)item{TextButton(onClick={vm.loadPublicSection("community",more=true)},enabled=!("community" in s.loading)){Text("Load more initiatives")}}
        }
    }
}

@Composable internal fun CivicScreen(vm:AppViewModel,s:AppState,nav:NavHostController) {
    LaunchedEffect(Unit){vm.loadPublicSection("civic");vm.loadPublicSection("advocacy")}
    var selected by remember{mutableStateOf<Advocacy?>(null)}
    selected?.let{a->ModalBottomSheet(sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),onDismissRequest={selected=null}){Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)){Text(a.title,style=MaterialTheme.typography.headlineSmall);listOf("Issue" to a.issue,"Background" to a.background,"KPN position" to a.kpn_position,"Actions taken" to a.actions_taken,"Government response" to a.government_response,"Outcome" to a.outcome).filter{it.second.isNotBlank()}.forEach{(label,body)->SectionHeading(label);ReadableBody(body)};Spacer(Modifier.height(24.dp))}}}
    LazyColumn(Modifier.fillMaxSize()) {
        item{KpnTopBar("Civic & advocacy",back={nav.popBackStack()});Loading(("advocacy" in s.loading));ErrorState(s.errors["advocacy"] ?: s.errors["civic"]){vm.loadPublicSection("advocacy");vm.loadPublicSection("civic")}}
        item{Column(Modifier.padding(horizontal=20.dp)){SectionHeading("Active advocacy")}}
        if(s.advocacy.isEmpty()&&!("advocacy" in s.loading)&&s.errors["advocacy"]==null)item{EmptyState("No active advocacy campaigns.")}
        items(s.advocacy,key={it.id}){a->Column(Modifier.padding(horizontal=20.dp)){ActionRow(a.title,a.issue){selected=a}}}
        item{Column(Modifier.padding(horizontal=20.dp)){SectionHeading("Civic newsroom")}}
        items(s.civic,key={"civic_${it.id}"}){a->EditorialRow(a){nav.navigate("article/${a.slug}")}}
        if(s.hasMore["advocacy"]==true)item{TextButton(onClick={vm.loadPublicSection("advocacy",more=true)}){Text("Load more advocacy")}}
        if(s.hasMore["civic"]==true)item{TextButton(onClick={vm.loadPublicSection("civic",more=true)}){Text("Load more civic stories")}}
    }
}

@Composable internal fun AdvocacyScreen(vm:AppViewModel,s:AppState,nav:NavHostController)=CivicScreen(vm,s,nav)

@Composable internal fun LeadershipScreen(vm:AppViewModel,s:AppState,nav:NavHostController) {
    var tier by remember{mutableStateOf<String?>(null)};var showFilters by remember{mutableStateOf(false)}
    var zone by remember{mutableStateOf<Zone?>(null)};var lga by remember{mutableStateOf<Lga?>(null)};var ward by remember{mutableStateOf<Ward?>(null)}
    var selected by remember{mutableStateOf<LeadershipSeat?>(null)}
    LaunchedEffect(showFilters){if(showFilters&&s.zones.isEmpty())vm.loadRegistrationData()};LaunchedEffect(tier,zone?.id,lga?.id,ward?.id){vm.loadLeadership(tier,zone?.id,lga?.id,ward?.id)}
    selected?.let{seat->LaunchedEffect(seat.holder?.id){seat.holder?.let{vm.loadPublicPerson(it.id)}};ModalBottomSheet(sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),onDismissRequest={selected=null}){Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)){KpnImage(seat.holder?.photo,seat.holder?.name.orEmpty(),Modifier.fillMaxWidth().heightIn(max=280.dp).aspectRatio(1f),portrait=true);Text(seat.holder?.name ?: "Vacant",style=MaterialTheme.typography.headlineSmall,modifier=Modifier.padding(top=16.dp));Text(seat.role_title,style=MaterialTheme.typography.titleMedium);Text(seat.location.label,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);s.publicPerson?.let{if(it.bio.isNotBlank()){SectionHeading("Profile");ReadableBody(it.bio)}};ErrorState(s.errors["person"]){seat.holder?.let{vm.loadPublicPerson(it.id)}};Spacer(Modifier.height(24.dp))}}}
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Leadership",back={nav.popBackStack()},actions={IconButton(onClick={showFilters=!showFilters}){Icon(Icons.Default.Tune,"Filter leadership")}})
        if(showFilters)ModalBottomSheet(onDismissRequest={showFilters=false},sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true)) { Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
            SectionHeading("Leadership filters","Choose the organizational location")
            KpnDropdown("Senatorial zone",zone,s.zones,{it.name},{zone=it;lga=null;ward=null;vm.loadLgas(it.id)})
            KpnDropdown("LGA",lga,s.lgas,{it.name},{lga=it;ward=null;vm.loadWards(it.id)},enabled=zone!=null)
            KpnDropdown("Ward",ward,s.wards,{it.name},{ward=it},enabled=lga!=null)
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){TextButton(onClick={zone=null;lga=null;ward=null;tier=null}){Text("Clear filters")};Button(onClick={showFilters=false},shape=MaterialTheme.shapes.small){Text("Done")}}
            Spacer(Modifier.height(24.dp))
        } }
        LazyRow(contentPadding=PaddingValues(horizontal=20.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){items(listOf("All" to null,"State" to "STATE","Senatorial" to "ZONAL","LGA" to "LGA","Ward" to "WARD")){(label,value)->FilterChip(selected=tier==value,onClick={tier=value},label={Text(label)})}}
        Loading(("leadership" in s.loading));ErrorState(s.errors["leadership"]){vm.loadLeadership(tier,zone?.id,lga?.id,ward?.id,force=true)}
        LazyVerticalGrid(columns=GridCells.Adaptive(if(androidx.compose.ui.platform.LocalDensity.current.fontScale>1.4f)220.dp else 148.dp),contentPadding=PaddingValues(20.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
            gridItems(s.leadership,key={it.seat_key}){seat->Column(Modifier.clickable{selected=seat}){KpnImage(seat.holder?.photo,seat.holder?.name ?: "Vacant leadership seat",Modifier.fillMaxWidth().aspectRatio(1f),portrait=true);Text(seat.holder?.name ?: "Vacant",style=MaterialTheme.typography.titleSmall,modifier=Modifier.padding(top=10.dp));Text(seat.role_title,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.primary);Text(seat.location.label,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
            if(s.hasMore["leadership"]==true)item(span={androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan)}){TextButton(onClick={vm.loadLeadership(tier,zone?.id,lga?.id,ward?.id,true)},enabled=!("leadership" in s.loading),modifier=Modifier.fillMaxWidth()){Text("Load more leaders")}}
            if(s.leadership.isEmpty()&&!("leadership" in s.loading)&&s.errors["leadership"]==null)item(span={androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan)}){EmptyState("No published leaders match these filters.")}
        }
    }
}
@Composable internal fun ImpactScreen(vm:AppViewModel,s:AppState,nav:NavHostController) {
    LaunchedEffect(Unit){vm.loadImpact()}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(bottom=24.dp)) {
        item{KpnTopBar("KPN impact",back={nav.popBackStack()});Loading(("impact" in s.loading));ErrorState(s.errors["impact"]){vm.loadImpact()}}
        s.impact?.let{i->item{Column(Modifier.padding(20.dp)){SectionHeading("Verified network","Figures reported by the KPN backend");LiveMetricPair(i.verified_members.toString(),"Verified members",i.trusted_reporters.toString(),"Trusted reporters");LiveMetricPair(i.active_lgas.toString(),"Active LGAs",i.active_wards.toString(),"Active wards");SectionHeading("Community reach");LiveMetricPair(i.community_reports.toString(),"Approved reports",i.impact_communities_reached.toString(),"Communities reached");SectionHeading("Organizational structure");listOf("Senatorial zones" to i.zones_total,"LGAs" to i.lgas_total,"Wards" to i.wards_total,"Open opportunities" to i.opportunities).forEach{(label,value)->Row(Modifier.fillMaxWidth().padding(vertical=12.dp)){Text(label,Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium);Text(value.toString(),style=MaterialTheme.typography.titleMedium)};KpnDivider()}}}}
    }
}
@Composable internal fun PatronsScreen(vm:AppViewModel,s:AppState,nav:NavHostController) {
    LaunchedEffect(Unit){vm.loadPublicSection("patrons")};var selected by remember{mutableStateOf<Patron?>(null)}
    selected?.let{p->ModalBottomSheet(sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),onDismissRequest={selected=null}){Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)){KpnImage(p.photo,p.full_name,Modifier.fillMaxWidth().heightIn(max=280.dp).aspectRatio(1f),portrait=true);Text(p.full_name,style=MaterialTheme.typography.headlineSmall,modifier=Modifier.padding(top=16.dp));Text(p.title,style=MaterialTheme.typography.titleSmall);SectionHeading("Biography");if(p.bio.isBlank())Text("No biography has been published.") else ReadableBody(p.bio);Spacer(Modifier.height(24.dp))}}}
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Our patrons",back={nav.popBackStack()});Loading(("patrons" in s.loading));ErrorState(s.errors["patrons"]){vm.loadPublicSection("patrons")}
        LazyVerticalGrid(columns=GridCells.Adaptive(if(androidx.compose.ui.platform.LocalDensity.current.fontScale>1.4f)220.dp else 148.dp),contentPadding=PaddingValues(20.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
            val patrons=s.patrons.sortedWith(compareBy({!it.isGrandPatron},{it.order}))
            patrons.filter{it.isGrandPatron}.forEach{p->item(key=p.id,span={androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan)}){Column(Modifier.clickable{selected=p}){Text("GRAND PATRON",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.primary);Spacer(Modifier.height(12.dp));KpnImage(p.photo,p.full_name,Modifier.fillMaxWidth().height(260.dp),portrait=true);Text(p.full_name,style=MaterialTheme.typography.titleLarge,modifier=Modifier.padding(top=12.dp));Text(p.title,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(p.bio,style=MaterialTheme.typography.bodySmall,maxLines=3,overflow=TextOverflow.Ellipsis,modifier=Modifier.padding(top=8.dp));TextButton(onClick={selected=p}){Text("Read biography")};KpnDivider()}}}
            gridItems(patrons.filter{!it.isGrandPatron},key={it.id}){p->Column(Modifier.clickable{selected=p}){KpnImage(p.photo,p.full_name,Modifier.fillMaxWidth().aspectRatio(1f),portrait=true);Text(p.full_name,style=MaterialTheme.typography.titleSmall,modifier=Modifier.padding(top=10.dp));Text(p.title,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(p.bio,style=MaterialTheme.typography.bodySmall,maxLines=2,overflow=TextOverflow.Ellipsis,modifier=Modifier.padding(top=8.dp));TextButton(onClick={selected=p},contentPadding=PaddingValues(0.dp)){Text("Read more")}}}
            if(s.hasMore["patrons"]==true)item(span={androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan)}){TextButton(onClick={vm.loadPublicSection("patrons",more=true)},enabled=!("patrons" in s.loading)){Text("Load more patrons")}}
            if(patrons.isEmpty()&&!("patrons" in s.loading)&&s.errors["patrons"]==null)item(span={androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan)}){EmptyState("KPN patrons will appear here when published.")}
        }
    }
}
// ── Public More ────────────────────────────────────────────────────────────────

@Composable
internal fun PublicMoreScreen(vm: AppViewModel, s: AppState, nav: NavHostController) {
    val columns=publicDirectoryColumns()
    LazyColumn(contentPadding=PaddingValues(bottom=24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { KpnTopBar("More") }
        item { OutlinedCard(modifier=Modifier.fillMaxWidth().padding(horizontal=20.dp),shape=MaterialTheme.shapes.medium,border=androidx.compose.foundation.BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.AccountCircle,null,Modifier.size(28.dp),tint=MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) { Text("Your KPN account",style=MaterialTheme.typography.titleMedium);Text("Sign in to access your membership and assigned work.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onPrimaryContainer) }
                }
                KpnPrimaryButton("Sign in",false){nav.navigate("login")}
            }
        } }
        publicDirectoryGroups.forEach { group ->
            item { Text(group.title,style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(horizontal=20.dp).padding(top=12.dp,bottom=4.dp)) }
            items(group.entries.chunked(columns)) { row -> Box(Modifier.padding(horizontal=20.dp)){PublicDirectoryRow(row){nav.navigate(it)}} }
        }
    }
}

// ── Content / About / FAQ / Gallery / Contact ──────────────────────────────────

@Composable
internal fun ContentScreen(vm: AppViewModel, s: AppState, nav: NavHostController, key: String, title: String) {
    val canonical = rememberCanonicalCopy(if(key=="code")"conduct" else key)
    LaunchedEffect(key) { vm.loadContent(key) }
    Column(Modifier.fillMaxSize()) {
        KpnTopBar(title, back = { nav.popBackStack() })
        Loading(("content" in s.loading))
        ErrorState(s.errors[key]) { vm.loadContent(key,force=true) }
        if(canonical==null) Loading(true)
        if(canonical!=null) {
            LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                items(canonical.blocks) { block ->
                    CanonicalBlock(block)
                    if(key=="support" && block.text=="Donation Account Details") {
                        // The live verification warning belongs with donation information, not in the footer.
                        s.contentPage?.message?.let { Spacer(Modifier.height(12.dp));InfoBanner(it,isWarning=true) }
                    }
                }
            }
        } else s.contentPage?.let { p ->
            Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
                p.motto?.let       { Text(it, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
                p.description?.let { Spacer(Modifier.height(8.dp)); Text(it, style = MaterialTheme.typography.bodyLarge) }
                p.vision?.let      { Spacer(Modifier.height(12.dp)); Text("Vision", style = MaterialTheme.typography.titleMedium); Text(it) }
                p.mission?.let     { Spacer(Modifier.height(12.dp)); Text("Mission", style = MaterialTheme.typography.titleMedium); Text(it) }
                p.message?.let     { Spacer(Modifier.height(12.dp)); Text(it) }
                p.address?.let     { Spacer(Modifier.height(12.dp)); DashRow("V", "Address", it) }
                p.phone?.let       { DashRow("P", "Phone", it) }
                p.email?.let       { DashRow("E", "Email", it) }
                if (p.pillars.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Pillars", style = MaterialTheme.typography.titleMedium)
                    p.pillars.forEachIndexed { i, pillar ->
                        Text("${i + 1}. $pillar", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 3.dp))
                    }
                }
                p.sections.forEach { sec ->
                    Spacer(Modifier.height(16.dp))
                    Text(sec.title, style = MaterialTheme.typography.titleMedium)
                    sec.body?.let { Text(it, modifier = Modifier.padding(top = 4.dp)) }
                    sec.items.forEach { Text("• $it", modifier = Modifier.padding(vertical = 3.dp)) }
                }
            }
        }
    }
}

@Composable
internal fun FaqScreen(vm: AppViewModel, s: AppState, nav: NavHostController) {
    val canonical = rememberCanonicalCopy("faq")
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { vm.loadContent("faq") }
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("FAQ", back = { nav.popBackStack() })
        Loading("content" in s.loading)
        ErrorState(s.errors["faq"]) { vm.loadContent("faq",force=true) }
        LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            canonical?.let { page ->
                items(page.blocks.take(2)) { CanonicalBlock(it) }
                item { KpnField("Search FAQs…",query,{query=it},trailingIcon={Icon(Icons.Default.Search,null)}) }
                item { LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) { items(listOf("All" to null,"About KPN" to "about","Membership" to "membership","Leadership" to "leadership","Reporting" to "reporting")) { (label,key) -> FilterChip(selected=category==key,onClick={category=key},label={Text(label)}) } } }
                val questions=page.questions.filter { (category==null || it.category==category) && (query.isBlank() || (it.question+it.answer.joinToString { b -> b.text }).contains(query,ignoreCase=true)) }
                if(questions.isEmpty())item { EmptyState("No FAQs match your search.") }
                items(questions,key={it.question}) { q ->
                    var open by remember { mutableStateOf(false) }
                    OutlinedCard(onClick={open=!open},colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment=Alignment.CenterVertically) { Text(q.question,Modifier.weight(1f),style=MaterialTheme.typography.titleSmall);Icon(if(open)Icons.Default.ExpandLess else Icons.Default.ExpandMore,if(open)"Collapse answer" else "Expand answer") }
                            if(open) { Spacer(Modifier.height(12.dp));Column(verticalArrangement=Arrangement.spacedBy(8.dp)) { q.answer.forEach { CanonicalBlock(it) } } }
                        }
                    }
                }
                items(page.blocks.drop(2)) { CanonicalBlock(it) }
            }
        }
    }
}

@Composable
internal fun GalleryScreen(vm: AppViewModel, s: AppState, nav: NavHostController) {
    LaunchedEffect(Unit) { vm.loadContent("gallery") }
    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Gallery", back = { nav.popBackStack() });Loading("content" in s.loading);ErrorState(s.errors["gallery"]){vm.loadContent("gallery",force=true)}
        if (s.gallery.isEmpty()&&!("content" in s.loading)&&s.errors["gallery"]==null) {
            EmptyState("No approved media")
        } else {
            LazyVerticalGrid(
                columns             = GridCells.Fixed(2),
                contentPadding      = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(8.dp),
            ) {
                gridItems(s.gallery) { m ->
                    Card {
                        Column {
                            if (m.file != null) {
                                AsyncImage(m.file, m.title, Modifier.fillMaxWidth().height(140.dp), contentScale = ContentScale.Crop)
                            } else {
                                Box(Modifier.fillMaxWidth().height(140.dp).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.VideoLibrary, null, Modifier.size(40.dp))
                                }
                            }
                            Text(m.title, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(8.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ContactScreen(vm: AppViewModel, s: AppState, nav: NavHostController) {
    LaunchedEffect(Unit) { vm.loadContent("contact") }
    var name    by remember { mutableStateOf("") }
    var email   by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        KpnTopBar("Contact", back = { nav.popBackStack() })
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            s.contentPage?.let { p ->
                p.address?.let { DashRow("V", "Visit us", it) }
                p.phone?.let   { DashRow("P", "Call us",  it) }
                p.email?.let   { DashRow("E", "Email us", it) }
                KpnDivider()
                Spacer(Modifier.height(12.dp))
            }
            Text("Send us a message", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp))
            KpnField("Name", name, { name = it })
            KpnField("Email", email, { email = it }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email))
            KpnField("Message", message, { message = it }, lines = 5)
            ErrorState(s.errors["contact"]) { vm.clearError("contact") }
            if (s.contactSent) {
                InfoBanner("Your message was sent. We'll be in touch.",tone=BannerTone.SUCCESS)
                Spacer(Modifier.height(8.dp))
            }
            Button(shape=MaterialTheme.shapes.small, 
                onClick  = { vm.sendContact(name, email, message) },
                enabled  = name.isNotBlank() && email.isNotBlank() && message.isNotBlank() && !s.busy,
                modifier =(Modifier.fillMaxWidth()).heightIn(min=48.dp),
            ) { Text("Send message") }
        }
    }
}
