@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.kpn.mobile.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import com.squareup.moshi.Moshi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kpn.mobile.core.*

@Composable internal fun NativeArticleEditorScreen(vm:AppViewModel,s:AppState,nav:NavHostController,editingId:Int?) {
    var draftId by rememberSaveable(editingId){mutableStateOf(editingId)}
    var headline by rememberSaveable(editingId){mutableStateOf("")};var subheadline by rememberSaveable(editingId){mutableStateOf("")};var category by rememberSaveable(editingId){mutableStateOf("GENERAL")}
    var verification by rememberSaveable(editingId){mutableStateOf("DEVELOPING")};var credit by rememberSaveable(editingId){mutableStateOf("KPN_REPORTER")}
    var body by rememberSaveable(editingId){mutableStateOf("")};var location by rememberSaveable(editingId){mutableStateOf("")};var meta by rememberSaveable(editingId){mutableStateOf("")}
    var lgaId by rememberSaveable(editingId){mutableStateOf<Int?>(null)};var wardId by rememberSaveable(editingId){mutableStateOf<Int?>(null)}
    var blocks by rememberSaveable(editingId,stateSaver=ArticleBlocksSaver){mutableStateOf<List<ArticleEditorBlock>>(emptyList())};var removed by remember{mutableStateOf<Pair<Int,ArticleEditorBlock>?>(null)}
    var photo by rememberSaveable{mutableStateOf<Uri?>(null)};var uploadedPhoto by rememberSaveable{mutableStateOf<Uri?>(null)};var remotePhoto by rememberSaveable{mutableStateOf<String?>(null)}
    var pickingBlock by remember{mutableStateOf<String?>(null)};var blockMenu by remember{mutableStateOf(false)};var metadataExpanded by remember{mutableStateOf(false)}
    var dirty by rememberSaveable{mutableStateOf(false)};var leave by remember{mutableStateOf(false)};var saved by rememberSaveable{mutableStateOf(false)}
    val featurePicker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->if(uri!=null){photo=uri;dirty=true;saved=false}}
    val blockPicker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->val id=pickingBlock;if(uri!=null&&id!=null)vm.uploadArticleBodyImage(uri){url->if(url!=null){blocks=blocks.map{if(it.id==id)it.copy(url=url) else it};dirty=true;saved=false}}}
    var loadedId by rememberSaveable(editingId){mutableStateOf<Int?>(null)}
    LaunchedEffect(editingId){vm.loadEditorLocations();if(editingId!=null&&loadedId!=editingId)vm.loadDraftForEditing(editingId)}
    LaunchedEffect(s.selectedArticle?.id,editingId){val article=s.selectedArticle;if(editingId!=null&&article?.id==editingId&&loadedId!=editingId){loadedId=editingId;headline=article.title;subheadline=article.subheadline;category=article.category;verification=article.verification_status;credit=article.reporter_credit;location=article.location;meta=article.meta_description;lgaId=article.lga;wardId=article.ward;remotePhoto=article.featured_image ?: article.image;blocks=ArticleEditorBlock.fromArticle(article);body=if(blocks.isEmpty())article.content else "";article.lga?.let{vm.loadWards(it)};dirty=false}}
    BackHandler(enabled=dirty){leave=true}
    if(leave)AlertDialog(onDismissRequest={leave=false},title={Text("Discard unsaved changes?")},text={Text("The last saved draft remains in your KPN account. Changes made since saving will not be kept.")},confirmButton={TextButton(onClick={leave=false;nav.popBackStack()}){Text("Discard changes",color=MaterialTheme.colorScheme.error)}},dismissButton={TextButton(onClick={leave=false}){Text("Keep editing")}})
    fun change(){dirty=true;saved=false}
    fun payload(): Map<String, Any?> = mapOf("title" to headline,"subheadline" to subheadline,"category" to category,"verification_status" to verification,"reporter_credit" to credit,"location" to location,"lga" to lgaId,"ward" to wardId,"meta_description" to meta,"content" to (listOf(body)+blocks.map{it.text}).filter{it.isNotBlank()}.joinToString("\n\n"),"content_json" to editorPayload(body,blocks))
    fun save(submit:Boolean){vm.saveArticleEditor(draftId,payload(),photo?.takeIf{it!=uploadedPhoto}){article->article?.let{draftId=it.id;uploadedPhoto=photo;dirty=false;saved=true;if(submit)vm.submitArticleForReview(it.id){ok->if(ok)nav.popBackStack()}}}}
    Column(Modifier.fillMaxSize()) {
        KpnTopBar(if(editingId==null)"Write article" else "Edit article draft",back={if(dirty)leave=true else nav.popBackStack()})
        if(editingId!=null&&s.selectedArticle?.id!=editingId){Loading(s.busy);ErrorState(s.errors["article_compose"]){vm.loadDraftForEditing(editingId)};return@Column}
        val readOnly=editingId!=null&&s.selectedArticle?.status in listOf("PENDING","PUBLISHED")
        if(readOnly){InfoBanner("Published and pending articles cannot be edited. The server protects the editorial workflow.");return@Column}
        LazyColumn(Modifier.weight(1f).imePadding(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            item {
                FormSection("01","Editorial details")
                KpnField("Headline",headline,{headline=it;change()},error=s.errors["article_compose"]?.fields?.get("title"))
                KpnField("Subheadline",subheadline,{subheadline=it;change()},error=s.errors["article_compose"]?.fields?.get("subheadline"))
                KpnDropdown("Category",category,ContentChoices.news.map{it.first},{key->ContentChoices.news.first{it.first==key}.second},{category=it;change()})
                KpnDropdown("Information status",verification,ContentChoices.verification.map{it.first},{key->ContentChoices.verification.first{it.first==key}.second},{verification=it;change()})
                KpnDropdown("Reporter credit",credit,ContentChoices.reporterCredit.map{it.first},{key->ContentChoices.reporterCredit.first{it.first==key}.second},{credit=it;change()})
                if(photo!=null||remotePhoto!=null)KpnImage(photo?.toString() ?: remotePhoto,"Article featured image",Modifier.fillMaxWidth().aspectRatio(16f/9f))
                TextButton(onClick={featurePicker.launch("image/*")}){Icon(Icons.Default.AddPhotoAlternate,"Choose featured image");Spacer(Modifier.width(8.dp));Text(if(photo!=null||remotePhoto!=null)"Replace featured image" else "Add featured image")}
                TextButton(onClick={metadataExpanded=!metadataExpanded}){Text(if(metadataExpanded)"Hide location & search details" else "Location & search details")}
                if(metadataExpanded){KpnField("Location",location,{location=it;change()});KpnDropdown("LGA",s.lgas.firstOrNull{it.id==lgaId},s.lgas,{it.name},{lgaId=it.id;wardId=null;vm.loadWards(it.id);change()});if(lgaId!=null){KpnDropdown("Ward (optional)",s.wards.firstOrNull{it.id==wardId},s.wards,{it.name},{wardId=it.id;change()});TextButton(onClick={lgaId=null;wardId=null;change()}){Text("Clear LGA / ward")}};KpnField("Search description (up to 160 characters)",meta,{meta=it;change()},lines=3,error=s.errors["article_compose"]?.fields?.get("meta_description"))}
                FormSection("02","Story body","Write directly, or add native content blocks below. Existing website blocks and media are preserved.")
                KpnRichTextField(if(blocks.isEmpty())"Article body" else "Opening paragraph (optional)",body,{body=it;change()},lines=if(blocks.isEmpty())7 else 3,error=s.errors["article_compose"]?.fields?.get("content"))
            }
            itemsIndexed(blocks,key={_,block->block.id}){index,block->
                ArticleBlockEditor(block,index,blocks.size,onChange={updated->blocks=blocks.map{if(it.id==updated.id)updated else it};change()},onPickImage={pickingBlock=block.id;blockPicker.launch("image/*")},onMove={delta->val target=index+delta;if(target in blocks.indices){val updated=blocks.toMutableList();updated.removeAt(index);updated.add(target,block);blocks=updated;change()}},onRemove={removed=index to block;blocks=blocks.filter{it.id!=block.id};change()})
            }
            item {
                Box {OutlinedButton(modifier=Modifier.heightIn(min=48.dp), onClick={blockMenu=true},shape=MaterialTheme.shapes.small){Icon(Icons.Default.Add,"Add content block");Spacer(Modifier.width(8.dp));Text("Add content block")};DropdownMenu(expanded=blockMenu,onDismissRequest={blockMenu=false}){ArticleEditorBlock.types.forEach{(type,label)->DropdownMenuItem(text={Text(label)},onClick={blocks=blocks+ArticleEditorBlock(type=type);blockMenu=false;change()})}}}
                removed?.let{(index,block)->TextButton(onClick={val restored=blocks.toMutableList();restored.add(index.coerceAtMost(restored.size),block);blocks=restored;removed=null;change()}){Text("Undo removed block")}}
                ErrorState(s.errors["locations"]){vm.loadEditorLocations()};ErrorState(s.errors["article_image"]){vm.clearError("article_image")};ErrorState(s.errors["article_compose"]){vm.clearError("article_compose")}
                if(saved)InfoBanner("Draft saved to your KPN account.")
                val ready=headline.isNotBlank()&&(body.isNotBlank()||blocks.any{it.text.isNotBlank()||it.url.isNotBlank()})
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){OutlinedButton(shape=MaterialTheme.shapes.small, modifier=Modifier.weight(1f).heightIn(min=48.dp),enabled=headline.isNotBlank()&&!s.busy,onClick={save(false)}){Text("Save draft")};Button(shape=MaterialTheme.shapes.small, modifier=Modifier.weight(1f).heightIn(min=48.dp),enabled=ready&&!s.busy,onClick={save(true)}){Text("Submit review")}}
                Loading(s.busy);Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable private fun ArticleBlockEditor(block:ArticleEditorBlock,index:Int,total:Int,onChange:(ArticleEditorBlock)->Unit,onPickImage:()->Unit,onMove:(Int)->Unit,onRemove:()->Unit) {
    OutlinedCard(Modifier.fillMaxWidth(),shape=MaterialTheme.shapes.small,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically){Text("Block ${index+1}",style=MaterialTheme.typography.labelLarge,modifier=Modifier.weight(1f));IconButton(enabled=index>0,onClick={onMove(-1)}){Icon(Icons.Default.ArrowUpward,"Move block up")};IconButton(enabled=index<total-1,onClick={onMove(1)}){Icon(Icons.Default.ArrowDownward,"Move block down")};IconButton(onClick=onRemove){Icon(Icons.Default.Close,"Remove content block")}}
            val known=ArticleEditorBlock.types.any{it.first==block.type}
            if(!known){InfoBanner("This website block type is retained unchanged. Its original data will be preserved when saving.");if(block.text.isNotBlank())ReadableBody(block.text);return@Column}
            KpnDropdown("Block type",block.type,ArticleEditorBlock.types.map{it.first},{key->ArticleEditorBlock.types.first{it.first==key}.second},{onChange(block.copy(type=it))})
            when(block.type){
                "image"->{if(block.url.isNotBlank())KpnImage(block.url,block.caption.ifBlank{"Article body image"},Modifier.fillMaxWidth().height(160.dp),portrait=true);TextButton(onClick=onPickImage){Text(if(block.url.isBlank())"Upload image (up to 2 MB)" else "Replace image")};KpnField("Caption",block.caption,{onChange(block.copy(caption=it))})}
                "embed"->{KpnField("Embedded media URL (https://…)",block.url,{onChange(block.copy(url=it))});KpnField("Caption",block.caption,{onChange(block.copy(caption=it))});Text("Embedded media opens in the appropriate external app from the native reader.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
                "delimiter"->KpnDivider()
                else->{if(block.type=="header")KpnDropdown("Heading size",block.level,listOf(2,3,4),{"Heading $it"},{onChange(block.copy(level=it))});if(block.type=="warning")KpnField("Notice title",block.title,{onChange(block.copy(title=it))});KpnRichTextField(if(block.type in listOf("list","checklist"))"Items (one per line)" else "Text",block.text,{onChange(block.copy(text=it))},lines=4);if(block.type=="quote")KpnField("Attribution",block.caption,{onChange(block.copy(caption=it))});if(block.type=="list")Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=block.ordered,onCheckedChange={onChange(block.copy(ordered=it))});Text("Numbered list")};if(block.type=="checklist")block.text.lines().filter{it.isNotBlank()}.forEachIndexed{itemIndex,text->Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=block.checks.getOrNull(itemIndex) ?: false,onCheckedChange={value->val checks=MutableList(block.text.lines().filter{it.isNotBlank()}.size){block.checks.getOrNull(it) ?: false};checks[itemIndex]=value;onChange(block.copy(checks=checks))});Text(text,style=MaterialTheme.typography.bodyMedium,modifier=Modifier.weight(1f))}}}
            }
        }
    }
}

private val ArticleBlocksSaver=Saver<List<ArticleEditorBlock>,String>(
    save={Moshi.Builder().build().adapter(Any::class.java).toJson(it.map{block->block.payload()})},
    restore={json->
        @Suppress("UNCHECKED_CAST")
        (Moshi.Builder().build().adapter(List::class.java).fromJson(json) as? List<Map<String,Any?>>)?.map{ArticleEditorBlock.from(it)}.orEmpty()
    }
)
