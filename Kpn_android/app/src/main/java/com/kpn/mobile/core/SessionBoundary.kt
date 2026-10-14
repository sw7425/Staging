package com.kpn.mobile.core

/** A revoked session must never leave cached private rows available to the next account. */
internal fun stateAfterFailure(state:AppState,scope:String,error:Result.Err,access:String?):AppState =
    if(error.status==401 && access.isNullOrBlank()) AppState(booting=false,errors=mapOf(scope to error))
    else state.copy(busy=false,booting=false,errors=state.errors+(scope to error))

/** Ephemeral logout-only credentials. Never writes into encrypted application preferences. */
internal class RetirementTokens(private var a:String?,private var r:String?):TokenStore {
    @Synchronized override fun access()=a
    @Synchronized override fun refresh()=r
    @Synchronized override fun save(access:String,refresh:String?){a=access;if(refresh!=null)r=refresh}
    @Synchronized override fun clearTokens(){a=null;r=null}
}
