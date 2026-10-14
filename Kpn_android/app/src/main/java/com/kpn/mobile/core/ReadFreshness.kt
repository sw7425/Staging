package com.kpn.mobile.core

/** In-memory freshness only. No private API responses are persisted or shared across accounts. */
internal class ReadFreshness(private val now:()->Long={System.nanoTime()/1_000_000}) {
 private data class Stamp(val key:String,val time:Long)
 private val stamps=mutableMapOf<String,Stamp>()
 fun shouldFetch(scope:String,key:String,ttl:Long,force:Boolean=false):Boolean {
  val s=stamps[scope]
  return force || s==null || s.key!=key || now()-s.time>=ttl
 }
 fun succeeded(scope:String,key:String){stamps[scope]=Stamp(key,now())}
 fun clear(){stamps.clear()}
}

object SignedInNavigation {
 val rootRoutes=listOf("home","dashboard","workspace","news_member","more")
 fun publicRoute(signedIn:Boolean,route:String):String=if(!signedIn)route else when(route){
  "news"->"news_member";"opportunities"->"opportunities_member";"register"->"dashboard";else->route
 }
}
