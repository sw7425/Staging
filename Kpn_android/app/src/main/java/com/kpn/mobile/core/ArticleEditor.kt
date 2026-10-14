package com.kpn.mobile.core
import com.kpn.mobile.data.Article
import com.squareup.moshi.Moshi
import java.util.UUID

data class ArticleEditorBlock(val id:String=UUID.randomUUID().toString(),val type:String="paragraph",val text:String="",val caption:String="",val url:String="",val level:Int=2,val title:String="",val ordered:Boolean=false,val checked:Boolean=false,val checks:List<Boolean> = emptyList(),val original:Map<String,Any?> = emptyMap()) {
    fun payload():Map<String,Any?> {
        if(type !in types.map{it.first})return original
        @Suppress("UNCHECKED_CAST") val old=original["data"] as? Map<String,Any?> ?: emptyMap()
        val data=when(type){
            "header"->mapOf("text" to text,"level" to level)
            "quote"->mapOf("text" to text,"caption" to caption,"alignment" to (old["alignment"] ?: "left"))
            "image"->{@Suppress("UNCHECKED_CAST") val file=old["file"] as? Map<String,Any?> ?: emptyMap();mapOf("file" to (file+("url" to url)),"caption" to caption)}
            "list"->mapOf("style" to if(ordered)"ordered" else "unordered","items" to text.lines().filter{it.isNotBlank()}.mapIndexed { index,line ->
                @Suppress("UNCHECKED_CAST") val previous=(old["items"] as? List<*>)?.getOrNull(index) as? Map<String,Any?>
                if(previous==null)line else previous+((if("content" in previous)"content" else "text") to line)
            })
            "checklist"->mapOf("items" to text.lines().filter{it.isNotBlank()}.mapIndexed{index,item->mapOf("text" to item,"checked" to (checks.getOrNull(index) ?: checked))})
            "warning"->mapOf("title" to title,"message" to text)
            "embed"->mapOf("embed" to url,"caption" to caption,"service" to (old["service"] ?: "media"))
            "delimiter"->emptyMap()
            else->mapOf("text" to text)
        }
        // Unedited nested list/checklist structures retain their original shape and per-row state.
        val sameItems=type in listOf("list","checklist")&&original.isNotEmpty()&&from(original).let{it.text==text&&it.ordered==ordered&&it.checked==checked&&it.checks==checks}
        val merged=old+data
        val finalData=if(sameItems)merged+("items" to old["items"]) else merged
        return original+mapOf("id" to id,"type" to type,"data" to finalData)
    }
    companion object {
        val types=listOf("paragraph" to "Paragraph","header" to "Heading","list" to "List","quote" to "Quote","image" to "Image","delimiter" to "Divider","warning" to "Notice","checklist" to "Checklist","embed" to "Embedded media")
        fun from(raw:Map<String,Any?>):ArticleEditorBlock {
            val data=raw["data"] as? Map<*,*> ?: emptyMap<Any,Any>();val file=data["file"] as? Map<*,*>;val type=raw["type"]?.toString() ?: "paragraph"
            val text=if(type in listOf("list","checklist"))(data["items"] as? List<*>)?.joinToString("\n"){if(it is Map<*,*>)it["text"]?.toString() ?: it["content"]?.toString().orEmpty() else it.toString()}.orEmpty() else (data[if(type=="warning")"message" else "text"]?.toString().orEmpty())
            return ArticleEditorBlock(id=raw["id"]?.toString() ?: UUID.randomUUID().toString(),type=type,text=text,caption=data["caption"]?.toString().orEmpty(),url=file?.get("url")?.toString() ?: data["url"]?.toString() ?: data["embed"]?.toString().orEmpty(),level=(data["level"] as? Number)?.toInt() ?: 2,title=data["title"]?.toString().orEmpty(),ordered=data["style"]=="ordered",checked=(data["items"] as? List<*>)?.all{(it as? Map<*,*>)?.get("checked")==true}==true,checks=(data["items"] as? List<*>)?.map{(it as? Map<*,*>)?.get("checked")==true}.orEmpty(),original=raw)
        }
        fun fromArticle(article:Article):List<ArticleEditorBlock> {
            val structured=when(val raw=article.content_json){is Map<*,*>->raw;is String->runCatching{Moshi.Builder().build().adapter(Map::class.java).fromJson(raw)}.getOrNull();else->null}
            @Suppress("UNCHECKED_CAST") return (structured?.get("blocks") as? List<*>)?.mapNotNull{(it as? Map<String,Any?>)?.let(::from)}.orEmpty()
        }
    }
}
fun editorPayload(body:String,blocks:List<ArticleEditorBlock>):Map<String,Any?>? {
    if(blocks.isEmpty()&&body.isBlank())return null
    val rows=buildList{if(body.isNotBlank())add(mapOf("type" to "paragraph","data" to mapOf("text" to body)));addAll(blocks.map{it.payload()})}
    return mapOf("blocks" to rows)
}
