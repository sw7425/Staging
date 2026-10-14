package com.kpn.mobile.ui
internal const val PUBLIC_OPPORTUNITIES_LABEL = "Opportunities"
/** Public newsroom retains its selected tab when opening a story. Routes remain unchanged. */
internal fun publicTabSelected(route: String?, tabRoute: String): Boolean =
    route == tabRoute || (tabRoute == "news" && route?.startsWith("article/") == true)
