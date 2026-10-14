package com.kpn.mobile.core
import org.junit.Assert.*
import org.junit.Test
class ArticleEditorContractTest {
    @Test fun existingImageMetadataIsPreserved(){val raw=mapOf<String,Any?>("id" to "image1","type" to "image","tunes" to mapOf("stretch" to true),"data" to mapOf("file" to mapOf("url" to "https://www.kpn.com.ng/media/image.jpg","width" to 640),"caption" to "Official caption","withBorder" to true));assertEquals(raw,ArticleEditorBlock.from(raw).payload())}
    @Test fun unknownWebsiteBlockIsNotDestroyed(){val raw=mapOf<String,Any?>("type" to "future_block","data" to mapOf("custom" to "Official data"));assertEquals(raw,ArticleEditorBlock.from(raw).payload())}
    @Test fun checklistStatesSurviveAnUneditedRoundTrip(){val raw=mapOf<String,Any?>("id" to "check1","type" to "checklist","data" to mapOf("items" to listOf(mapOf("text" to "First","checked" to true),mapOf("text" to "Second","checked" to false))));assertEquals(raw,ArticleEditorBlock.from(raw).payload())}
    @Test fun openingParagraphIsIncludedOnlyOnce(){val data=editorPayload("Opening",listOf(ArticleEditorBlock(type="quote",text="Statement")))!!;val rows=data["blocks"] as List<*>;assertEquals(2,rows.size);assertEquals("paragraph",(rows.first() as Map<*,*>)["type"])}
}
