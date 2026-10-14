package com.kpn.mobile.core
import com.kpn.mobile.data.MemberDecision
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
class MemberDecisionTransportTest {
 private class Tokens:TokenStore {override fun access()="local-token";override fun refresh():String?=null;override fun save(access:String,refresh:String?){};override fun clearTokens(){} }
 private fun json(body:String,status:Int=200)=MockResponse().setHeader("Content-Type","application/json").setResponseCode(status).setBody(body)
 @Test fun actualRetrofitPostsDecisionWithBearerAndParsesVerified()=runBlocking {val server=MockWebServer();server.start();try{server.enqueue(json("""{"id":3,"username":"isolated","status":"VERIFIED"}"""));val client=ApiClient(Tokens(),server.url("/api/v1/").toString());val r=client.safe{client.api.decideMember(3,MemberDecision("approve"))};assertTrue(r is Result.Ok);assertEquals("VERIFIED",(r as Result.Ok).value.status);val req=server.takeRequest();assertEquals("/api/v1/members/3/decision/",req.path);assertEquals("POST",req.method);assertEquals("Bearer local-token",req.getHeader("Authorization"));assertEquals("""{"decision":"approve"}""",req.body.readUtf8())}finally{server.shutdown()}}
 @Test fun rejectionIsVisibleAndRetryUsesUnchangedEndpoint()=runBlocking {val server=MockWebServer();server.start();try{server.enqueue(json("""{"error":{"code":"tier_ceiling","message":"Only President or Vice President can approve state applicants.","details":{}}}""",403));server.enqueue(json("""{"id":3,"status":"VERIFIED"}"""));val client=ApiClient(Tokens(),server.url("/api/v1/").toString());val failed=client.safe{client.api.decideMember(3,MemberDecision("approve"))};assertTrue(failed is Result.Err);assertEquals(403,(failed as Result.Err).status);assertEquals("tier_ceiling",failed.code);assertTrue(failed.message.contains("President"));assertTrue(client.safe{client.api.decideMember(3,MemberDecision("approve"))} is Result.Ok)}finally{server.shutdown()}}
 @Test fun serverFailureRemainsFailure()=runBlocking {val server=MockWebServer();server.start();try{server.enqueue(json("{}",503));val client=ApiClient(Tokens(),server.url("/api/v1/").toString());val failed=client.safe{client.api.decideMember(3,MemberDecision("approve"))};assertTrue(failed is Result.Err);assertEquals(503,(failed as Result.Err).status)}finally{server.shutdown()}}
}
