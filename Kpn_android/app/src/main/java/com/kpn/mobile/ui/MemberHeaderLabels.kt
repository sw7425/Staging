package com.kpn.mobile.ui

/** Display-only tier caption. Geographic scope and authorization remain server-owned. */
internal fun memberHeaderTierLabel(tier: String): String = when (tier.uppercase()) {
    "STATE" -> "Executive"
    "ZONAL" -> "Zonal"
    "LGA" -> "LGA"
    "WARD" -> "Ward"
    else -> "KPN membership"
}
