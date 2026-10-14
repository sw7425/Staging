package com.kpn.mobile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Verified static website narrative only. Never inserted into API DTOs or application state. */
internal data class CopyBlock(val kind: String, val html: String, val text: String)
internal data class CopyQuestion(val category: String, val question: String, val answer: List<CopyBlock>)
internal data class CanonicalCopy(val blocks: List<CopyBlock>, val questions: List<CopyQuestion> = emptyList())
internal fun parseCanonicalCopy(json: String): CanonicalCopy {
    val raw = requireNotNull(Moshi.Builder().build().adapter(Map::class.java).fromJson(json))
    fun blocks(value: Any?): List<CopyBlock> = (value as? List<*>).orEmpty().map { item ->
        val block = item as Map<*, *>
        CopyBlock(block["kind"] as String, block["html"] as String, block["text"] as String)
    }
    return CanonicalCopy(blocks(raw["blocks"]), (raw["questions"] as? List<*>).orEmpty().map { item ->
        val q = item as Map<*, *>
        CopyQuestion(q["category"] as String, q["question"] as String, blocks(q["answer"]))
    })
}
@Composable internal fun rememberCanonicalCopy(key: String): CanonicalCopy? {
    val context = LocalContext.current.applicationContext
    var page by remember(key) { mutableStateOf<CanonicalCopy?>(null) }
    LaunchedEffect(key) {
        page = withContext(Dispatchers.IO) {
            runCatching { context.assets.open("kpn_public_copy/$key.json").bufferedReader().use { parseCanonicalCopy(it.readText()) } }.getOrNull()
        }
    }
    return page
}
@Composable internal fun CanonicalBlock(block: CopyBlock) {
    when (block.kind) {
        "heading" -> ReadableBody(block.html, MaterialTheme.typography.titleLarge)
        else -> ReadableBody(block.html)
    }
}
@Composable internal fun CanonicalNarrative(page: CanonicalCopy) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        page.blocks.forEach { block -> CanonicalBlock(block) }
    }
}
