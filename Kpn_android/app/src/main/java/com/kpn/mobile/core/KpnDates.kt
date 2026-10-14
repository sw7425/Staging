package com.kpn.mobile.core
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale
object KpnDates {
    private val zone=ZoneId.of("Africa/Lagos")
    private fun instant(value:String):ZonedDateTime?=runCatching{OffsetDateTime.parse(value).atZoneSameInstant(zone)}.getOrNull() ?: runCatching{LocalDateTime.parse(value).atZone(zone)}.getOrNull()
    fun dateTime(value:String?):String {if(value.isNullOrBlank())return "";return instant(value)?.format(DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm",Locale.ENGLISH)) ?: value}
    fun date(value:String?):String {if(value.isNullOrBlank())return "";val date=instant(value)?.toLocalDate() ?: runCatching{LocalDate.parse(value)}.getOrNull();return date?.format(DateTimeFormatter.ofPattern("d MMM yyyy",Locale.ENGLISH)) ?: value}
}
