package com.kpn.mobile.core
import java.net.URI
data class InlineEdit(val text:String,val cursor:Int)
fun wrapInline(text:String,start:Int,end:Int,tag:String):InlineEdit {
    require(tag in setOf("b","i"));val a=minOf(start,end).coerceIn(0,text.length);val b=maxOf(start,end).coerceIn(a,text.length)
    val opening="<$tag>";val replacement=opening+text.substring(a,b)+"</$tag>"
    return InlineEdit(text.substring(0,a)+replacement+text.substring(b),a+opening.length+if(a==b)0 else b-a)
}
fun insertInlineLink(text:String,start:Int,end:Int,url:String,label:String):InlineEdit {
    val uri=URI(url.trim());require(uri.scheme in setOf("https","http")&&uri.host!=null&&uri.userInfo==null){"Use a valid HTTP or HTTPS link."}
    fun escape(value:String)=value.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;")
    val a=minOf(start,end).coerceIn(0,text.length);val b=maxOf(start,end).coerceIn(a,text.length);val replacement="<a href=\"${escape(uri.toString())}\">${escape(label)}</a>"
    return InlineEdit(text.substring(0,a)+replacement+text.substring(b),a+replacement.length)
}
