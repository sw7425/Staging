@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.kpn.mobile.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

private val KpnTimeZone=ZoneId.of("Africa/Lagos")
@Composable internal fun KpnDateField(label:String,value:String,onChange:(String)->Unit,dateTime:Boolean=false,error:String?=null) {
    val parsed=runCatching{if(dateTime)OffsetDateTime.parse(value).atZoneSameInstant(KpnTimeZone).toLocalDateTime() else LocalDate.parse(value).atTime(9,0)}.getOrNull()
    var picking by remember{mutableStateOf(false)}
    if(picking){
        val state=rememberDatePickerState(initialSelectedDateMillis=parsed?.toLocalDate()?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli())
        DatePickerDialog(onDismissRequest={picking=false},confirmButton={TextButton(enabled=state.selectedDateMillis!=null,onClick={val date=Instant.ofEpochMilli(state.selectedDateMillis!!).atZone(ZoneOffset.UTC).toLocalDate();onChange(if(dateTime)date.atTime(parsed?.toLocalTime() ?: LocalTime.of(9,0)).atZone(KpnTimeZone).toOffsetDateTime().toString() else date.toString());picking=false}){Text("Set date")}},dismissButton={TextButton(onClick={picking=false}){Text("Cancel")}}){DatePicker(state=state)}
    }
    Column(Modifier.fillMaxWidth().padding(vertical=4.dp)) {
        Text(label,style=MaterialTheme.typography.labelLarge)
        OutlinedButton(onClick={picking=true},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),shape=MaterialTheme.shapes.small){Icon(Icons.Default.CalendarMonth,"Choose $label");Spacer(Modifier.width(8.dp));Text(parsed?.toLocalDate()?.format(DateTimeFormatter.ofPattern("d MMM yyyy",Locale.ENGLISH)) ?: "Choose date")}
        if(dateTime&&parsed!=null)KpnTimeField("$label time · Nigeria",parsed.toLocalTime().toString(),{time->onChange(parsed.toLocalDate().atTime(LocalTime.parse(time)).atZone(KpnTimeZone).toOffsetDateTime().toString())})
        if(value.isNotBlank())TextButton(onClick={onChange("")}){Text("Clear $label")}
        if(error!=null)Text(error,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)
    }
}
@Composable internal fun KpnTimeField(label:String,value:String,onChange:(String)->Unit,error:String?=null) {
    var choosing by remember{mutableStateOf(false)}
    val parsed=runCatching{LocalTime.parse(value)}.getOrNull()
    if(choosing){val state=rememberTimePickerState(initialHour=parsed?.hour ?: 9,initialMinute=parsed?.minute ?: 0,is24Hour=true)
        AlertDialog(onDismissRequest={choosing=false},title={Text(label)},text={TimePicker(state=state,layoutType=TimePickerLayoutType.Vertical)},confirmButton={TextButton(onClick={onChange(LocalTime.of(state.hour,state.minute).toString());choosing=false}){Text("Set time")}},dismissButton={TextButton(onClick={choosing=false}){Text("Cancel")}})
    }
    Column(Modifier.fillMaxWidth().padding(vertical=4.dp)){Text(label,style=MaterialTheme.typography.labelLarge);OutlinedButton(onClick={choosing=true},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),shape=MaterialTheme.shapes.small){Icon(Icons.Default.Schedule,"Choose $label");Spacer(Modifier.width(8.dp));Text(parsed?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "Choose time")};if(error!=null)Text(error,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)}
}
