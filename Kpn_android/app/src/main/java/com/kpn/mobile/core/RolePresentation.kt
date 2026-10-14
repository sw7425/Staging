package com.kpn.mobile.core

/** Resolve the uploaded website's contextual template text without changing its responsibilities. */
fun roleResponsibility(text:String,lga:String?,ward:String?):String = text
    .replace("{{ user.lga.name }}",lga?.takeIf { it.isNotBlank() } ?: "your assigned")
    .replace("{{ user.ward.name }}",ward?.takeIf { it.isNotBlank() } ?: "your assigned")
    .replace(Regex("^\\d+\\s+"),"")
fun membershipTierLabel(tier:String):String=when(tier){"GENERAL"->"General Member";"STATE"->"State";"ZONAL"->"Senatorial";"LGA"->"LGA";"WARD"->"Ward";else->tier}

fun dashboardJurisdiction(value:String?,fallback:String):String=value?.takeUnless{it.isBlank()||it=="N/A"} ?: fallback
