package com.kpn.mobile.core

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Device appearance only. Kept separate from encrypted authentication/session storage. */
enum class AppearanceChoice(val label:String) {
    SYSTEM("System default"), LIGHT("Light"), DARK("Dark");
    fun isDark(systemDark:Boolean):Boolean=when(this){SYSTEM->systemDark;LIGHT->false;DARK->true}
    companion object { fun fromStored(value:String?):AppearanceChoice=entries.firstOrNull{it.name==value} ?: SYSTEM }
}
class AppearanceStore(context:Context) {
    private val preferences=context.applicationContext.getSharedPreferences("kpn_appearance",Context.MODE_PRIVATE)
    private val mutableChoice=MutableStateFlow(AppearanceChoice.fromStored(preferences.getString("choice",null)))
    val choice:StateFlow<AppearanceChoice> = mutableChoice.asStateFlow()
    fun setChoice(value:AppearanceChoice) {
        preferences.edit().putString("choice",value.name).apply()
        mutableChoice.value=value
    }
}
