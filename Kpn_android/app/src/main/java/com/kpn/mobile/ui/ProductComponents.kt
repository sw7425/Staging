package com.kpn.mobile.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import android.content.Intent
import android.net.Uri
import android.text.style.URLSpan
import android.text.style.StyleSpan
import android.graphics.Typeface
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.TextStyle

@Composable internal fun SectionHeading(title: String, supporting: String? = null, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            supporting?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        if (action != null) TextButton(onClick = onAction) { Text(action) }
    }
}

/** Editorial/portrait regions use real remote images with a deliberate, accessible empty fallback. */
@Composable internal fun KpnImage(url: String?, description: String, modifier: Modifier = Modifier, portrait: Boolean = false) {
    val fallback: @Composable () -> Unit = {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            Icon(if (portrait) Icons.Default.PersonOutline else Icons.Default.ImageNotSupported, description, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
        }
    }
    Box(modifier.clip(MaterialTheme.shapes.small)) {
        if (url.isNullOrBlank()) fallback() else SubcomposeAsyncImage(model = url, contentDescription = description,
            modifier = Modifier.fillMaxSize(), contentScale = if (portrait) ContentScale.Fit else ContentScale.Crop,
            loading = { Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp) } }, error = { fallback() })
    }
}

@Composable internal fun ActionRow(title: String, supporting: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).heightIn(min = 48.dp).padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleSmall); Text(supporting, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, "Open $title", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
    }
    KpnDivider()
}
@Composable internal fun FormSection(number: String, title: String, supporting: String? = null) {
    Row(Modifier.padding(top = 18.dp, bottom = 14.dp), verticalAlignment = Alignment.Top) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(4.dp)) { Text(number, Modifier.padding(8.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.width(12.dp)); Column { Text(title, style = MaterialTheme.typography.titleMedium); supporting?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    }
}
@Composable internal fun KpnPrimaryButton(text: String, busy: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled && !busy, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        if (busy) { CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary); Spacer(Modifier.width(10.dp)) }
        Text(if (busy) "Please wait…" else text)
    }
}
@Composable internal fun ReadableBody(text: String, style:TextStyle=MaterialTheme.typography.bodyLarge) {
    val context=LocalContext.current
    val spanned=remember(text){androidx.core.text.HtmlCompat.fromHtml(text,androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY)}
    val linkColor=MaterialTheme.colorScheme.primary
    val content=remember(spanned,linkColor){buildAnnotatedString{
        append(spanned.toString().trimEnd())
        val size=length
        spanned.getSpans(0,spanned.length,StyleSpan::class.java).forEach{span->
            val start=spanned.getSpanStart(span).coerceIn(0,size);val end=spanned.getSpanEnd(span).coerceIn(start,size)
            if(end>start)addStyle(SpanStyle(fontWeight=if(span.style==Typeface.BOLD||span.style==Typeface.BOLD_ITALIC)FontWeight.Bold else null,fontStyle=if(span.style==Typeface.ITALIC||span.style==Typeface.BOLD_ITALIC)FontStyle.Italic else null),start,end)
        }
        spanned.getSpans(0,spanned.length,URLSpan::class.java).forEach{span->
            val uri=runCatching{java.net.URI("https://www.kpn.com.ng/").resolve(span.url)}.getOrNull()
            val start=spanned.getSpanStart(span).coerceIn(0,size);val end=spanned.getSpanEnd(span).coerceIn(start,size)
            if(uri!=null&&uri.scheme in listOf("https","http")&&uri.host!=null&&end>start)addLink(LinkAnnotation.Url(uri.toString(),TextLinkStyles(style=SpanStyle(color=linkColor,textDecoration=TextDecoration.Underline)),linkInteractionListener={runCatching{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(uri.toString())))}}),start,end)
        }
    }}
    Text(content,style=style)
}

/** Mirrors the actual Editor.js renderer in campaigns/templatetags/campaign_extras.py. */
@Composable internal fun ArticleBody(article:com.kpn.mobile.data.Article) {
    val structured=remember(article.content_json){when(val raw=article.content_json){is Map<*,*>->raw;is String->runCatching{com.squareup.moshi.Moshi.Builder().build().adapter(Map::class.java).fromJson(raw)}.getOrNull();else->null}}
    val blocks=(structured?.get("blocks") as? List<*>)?.mapNotNull{it as? Map<*,*>}.orEmpty()
    if(blocks.isEmpty()){ReadableBody(article.content);return}
    val context=LocalContext.current
    Column(verticalArrangement=Arrangement.spacedBy(16.dp)) {
        blocks.forEach { block ->
            val data=block["data"] as? Map<*,*> ?: emptyMap<Any,Any>()
            fun value(key:String)=data[key]?.toString().orEmpty()
            when(block["type"]?.toString()) {
                "paragraph"->ReadableBody(value("text"))
                "header"->ReadableBody(value("text"),style=when((data["level"] as? Number)?.toInt()){3->MaterialTheme.typography.titleLarge;4->MaterialTheme.typography.titleMedium;else->MaterialTheme.typography.headlineSmall})
                "list"->ArticleListItems(data["items"] as? List<*> ?: emptyList<Any>(),value("style")=="ordered")
                "image"->{val file=data["file"] as? Map<*,*>;val url=file?.get("url")?.toString() ?: value("url");if(url.isNotBlank()){val absolute=runCatching{java.net.URI("https://www.kpn.com.ng/").resolve(url).toString()}.getOrDefault(url);KpnImage(absolute,value("caption").ifBlank{"Article image"},Modifier.fillMaxWidth().aspectRatio(4f/3f),portrait=true);if(value("caption").isNotBlank())ReadableBody(value("caption"),style=MaterialTheme.typography.bodySmall)}}
                "quote"->Surface(color=MaterialTheme.colorScheme.surfaceVariant,shape=MaterialTheme.shapes.small){Column(Modifier.padding(16.dp)){ReadableBody(value("text"));if(value("caption").isNotBlank()){Spacer(Modifier.height(8.dp));ReadableBody("— "+value("caption"),style=MaterialTheme.typography.bodySmall)}}}
                "delimiter"->KpnDivider()
                "warning"->Column{ReadableBody(value("title"),style=MaterialTheme.typography.titleSmall);ReadableBody(value("message"))}
                "checklist"->Column{(data["items"] as? List<*>)?.forEach{entry->val item=entry as? Map<*,*>;Row(verticalAlignment=Alignment.Top){Icon(if(item?.get("checked")==true)Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,if(item?.get("checked")==true)"Completed" else "Not completed",Modifier.size(20.dp));Spacer(Modifier.width(8.dp));ReadableBody(item?.get("text")?.toString() ?: entry.toString())}}}
                "embed"->{val url=value("embed");if(url.isNotBlank()){val uri=runCatching{Uri.parse(url)}.getOrNull();if(uri?.scheme in listOf("https","http"))OutlinedButton(shape=MaterialTheme.shapes.small, modifier=Modifier.heightIn(min=48.dp), onClick={runCatching{context.startActivity(Intent(Intent.ACTION_VIEW,uri))}}){Icon(Icons.Default.OpenInNew,"Open embedded media");Spacer(Modifier.width(8.dp));Text(value("caption").ifBlank{"Open ${value("service").ifBlank{"embedded media"}}"})};if(value("caption").isNotBlank())ReadableBody(value("caption"))}}
                else->if(value("text").isNotBlank())ReadableBody(value("text"))
            }
        }
    }
}

@Composable private fun ArticleListItems(entries:List<*>,ordered:Boolean,depth:Int=0) {
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
        entries.forEachIndexed { index,entry ->
            val item=entry as? Map<*,*>
            Column {
                Row { Text(if(ordered)"${index+1}. " else "• ",style=MaterialTheme.typography.bodyLarge)
                    Box(Modifier.weight(1f)){ReadableBody(item?.get("content")?.toString() ?: item?.get("text")?.toString() ?: entry.toString())}
                }
                val children=item?.get("items") as? List<*>
                if(!children.isNullOrEmpty()&&depth<12) { Spacer(Modifier.height(8.dp));Box(Modifier.padding(start=16.dp)){ArticleListItems(children,ordered,depth+1)} }
            }
        }
    }
}
