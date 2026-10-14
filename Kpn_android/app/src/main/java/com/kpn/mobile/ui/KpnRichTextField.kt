package com.kpn.mobile.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.kpn.mobile.core.*

/** Native selection-based formatting using the same inline HTML supported by the website's Editor.js renderer. */
@Composable internal fun KpnRichTextField(label:String,value:String,onChange:(String)->Unit,lines:Int=4,error:String?=null) {
    var field by rememberSaveable(stateSaver=TextFieldValue.Saver){mutableStateOf(TextFieldValue(value))}
    var preview by rememberSaveable{mutableStateOf(false)};var linking by remember{mutableStateOf(false)}
    var linkUrl by remember{mutableStateOf("")};var linkLabel by remember{mutableStateOf("")};var linkError by remember{mutableStateOf<String?>(null)}
    LaunchedEffect(value){if(field.text!=value)field=TextFieldValue(value,TextRange(value.length))}
    fun apply(edit:InlineEdit){field=TextFieldValue(edit.text,TextRange(edit.cursor));onChange(edit.text)}
    if(linking)AlertDialog(onDismissRequest={linking=false},title={Text("Insert link")},text={Column{KpnField("Link text",linkLabel,{linkLabel=it});KpnField("HTTPS URL",linkUrl,{linkUrl=it;linkError=null},error=linkError)}},confirmButton={TextButton(enabled=linkLabel.isNotBlank(),onClick={runCatching{insertInlineLink(field.text,field.selection.start,field.selection.end,linkUrl,linkLabel)}.onSuccess{apply(it);linking=false}.onFailure{linkError="Enter a valid HTTP or HTTPS URL."}}){Text("Insert")}},dismissButton={TextButton(onClick={linking=false}){Text("Cancel")}})
    Column(Modifier.fillMaxWidth().padding(vertical=4.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically){
            IconButton(enabled=!preview,onClick={apply(wrapInline(field.text,field.selection.start,field.selection.end,"b"))}){Icon(Icons.Default.FormatBold,"Bold selected text")}
            IconButton(enabled=!preview,onClick={apply(wrapInline(field.text,field.selection.start,field.selection.end,"i"))}){Icon(Icons.Default.FormatItalic,"Italic selected text")}
            IconButton(enabled=!preview,onClick={linkLabel=field.text.substring(field.selection.min,field.selection.max);linkUrl="";linkError=null;linking=true}){Icon(Icons.Default.Link,"Insert a link")}
            Spacer(Modifier.weight(1f));TextButton(onClick={preview=!preview}){Text(if(preview)"Write" else "Preview")}
        }
        if(preview){Text(label,style=MaterialTheme.typography.labelLarge);Spacer(Modifier.height(8.dp));ReadableBody(field.text.ifBlank{"No text yet."})}
        else OutlinedTextField(value=field,onValueChange={val changed=field.text!=it.text;field=it;if(changed)onChange(it.text)},label={Text(label)},modifier=Modifier.fillMaxWidth(),minLines=lines,maxLines=lines+8,isError=error!=null,supportingText=if(error!=null){{Text(error)}} else null,shape=MaterialTheme.shapes.small)
    }
}
