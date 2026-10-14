package com.kpn.mobile.core

import com.kpn.mobile.data.LoginRequest
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

private class TestTokens(var accessValue:String?=null,var refreshValue:String?=null):TokenStore {
    @Synchronized override fun access()=accessValue
    @Synchronized override fun refresh()=refreshValue
    @Synchronized override fun save(access:String,refresh:String?){accessValue=access;if(refresh!=null)refreshValue=refresh}
    @Synchronized override fun clearTokens(){accessValue=null;refreshValue=null}
}
class AuthenticationTransportTest {
    private fun delayedRefreshCannotChangeNewSession(success:Boolean,logout:Boolean)=runBlocking {
        val server=MockWebServer();val store=TestTokens("old-access","old-refresh")
        server.dispatcher=object:okhttp3.mockwebserver.Dispatcher(){
            override fun dispatch(request:okhttp3.mockwebserver.RecordedRequest):MockResponse {
                if(request.path=="/api/v1/auth/refresh/") {
                    if(logout)store.clearTokens() else store.save("new-account-access","new-account-refresh")
                    return if(success)json("""{"access":"late-old-access","refresh":"late-old-refresh"}""") else json("{}").setResponseCode(401)
                }
                return json("{}").setResponseCode(401)
            }
        }
        server.start()
        try {
            val client=ApiClient(store,server.url("/api/v1/").toString());val result=client.safe{client.api.me()}
            assertTrue(result is Result.Err);assertEquals(2,server.requestCount)
            assertEquals(if(logout)null else "new-account-access",store.access())
            assertEquals(if(logout)null else "new-account-refresh",store.refresh())
        }finally{server.shutdown()}
    }
    @Test fun delayedRefreshCannotRestoreLoggedOutSession(){delayedRefreshCannotChangeNewSession(true,true)}
    @Test fun delayedRefreshCannotOverwriteNewAccount(){delayedRefreshCannotChangeNewSession(true,false)}
    @Test fun oldInvalidRefreshCannotClearNewAccount(){delayedRefreshCannotChangeNewSession(false,false)}

    private fun json(value:String)=MockResponse().setHeader("Content-Type","application/json").setBody(value)
    @Test fun loginPostStoresTokensAndMeSendsBearerHeader()=runBlocking {
        val server=MockWebServer();server.start()
        try {
            server.enqueue(json("""{"access":"test-access","refresh":"test-refresh"}"""))
            server.enqueue(json("""{"id":7,"username":"test_member","status":"VERIFIED","access":{"state":"ALLOWED","telegram_required":true,"telegram_verified":true}}"""))
            val store=TestTokens();val client=ApiClient(store,server.url("/api/v1/").toString())
            val pair=client.api.login(LoginRequest("test_member","test-only-password"));store.save(pair.access,pair.refresh)
            val me=client.api.me();assertEquals(7,me.id);assertTrue(me.access.telegram_verified)
            val login=server.takeRequest();assertEquals("POST",login.method);assertEquals("/api/v1/auth/login/",login.path);assertNull(login.getHeader("Authorization"))
            val identity=server.takeRequest();assertEquals("Bearer test-access",identity.getHeader("Authorization"))
        } finally {server.shutdown()}
    }
    @Test fun expiredAccessRotatesRefreshAndRetriesOnce()=runBlocking {
        val server=MockWebServer();server.start()
        try {
            server.enqueue(json("""{"error":{"code":"authentication_failed","message":"Token expired"}}""").setResponseCode(401))
            server.enqueue(json("""{"access":"rotated-access","refresh":"rotated-refresh"}"""))
            server.enqueue(json("""{"id":7,"status":"VERIFIED"}"""))
            val store=TestTokens("expired","initial-refresh");val client=ApiClient(store,server.url("/api/v1/").toString());assertEquals(7,client.api.me().id)
            assertEquals("Bearer expired",server.takeRequest().getHeader("Authorization"))
            val refresh=server.takeRequest();assertEquals("/api/v1/auth/refresh/",refresh.path);assertNull(refresh.getHeader("Authorization"));assertTrue(refresh.body.readUtf8().contains("initial-refresh"))
            assertEquals("Bearer rotated-access",server.takeRequest().getHeader("Authorization"));assertEquals("rotated-refresh",store.refresh())
        } finally {server.shutdown()}
    }
    @Test fun invalidRefreshClearsOnlyAuthenticationAndDoesNotLoop()=runBlocking {
        val server=MockWebServer();server.start()
        try {
            server.enqueue(json("{}").setResponseCode(401));server.enqueue(json("{}").setResponseCode(401))
            val store=TestTokens("expired","expired-refresh");val client=ApiClient(store,server.url("/api/v1/").toString());assertTrue(client.safe{client.api.me()} is Result.Err)
            assertNull(store.access());assertNull(store.refresh());assertEquals(2,server.requestCount)
        } finally {server.shutdown()}
    }
    @Test fun emptyTokenNeverProducesEmptyBearerHeader()=runBlocking {
        val server=MockWebServer();server.start()
        try {server.enqueue(json("{}").setResponseCode(401));val client=ApiClient(TestTokens("",null),server.url("/api/v1/").toString());client.safe{client.api.me()};assertNull(server.takeRequest().getHeader("Authorization"))} finally {server.shutdown()}
    }
    @Test fun redirectCannotSilentlyForwardCredentials()=runBlocking {
        val origin=MockWebServer();val other=MockWebServer();origin.start();other.start()
        try {
            origin.enqueue(MockResponse().setResponseCode(301).setHeader("Location",other.url("/api/v1/auth/me/")))
            val client=ApiClient(TestTokens("test-access",null),origin.url("/api/v1/").toString());val result=client.safe{client.api.me()}
            assertTrue(result is Result.Err);assertEquals(0,other.requestCount);assertEquals(301,(result as Result.Err).status)
        } finally {origin.shutdown();other.shutdown()}
    }
    @Test fun reproducesOldCrossHostRedirectAuthorizationLoss() {
        val origin=MockWebServer();val other=MockWebServer();origin.start();other.start()
        try {
            origin.enqueue(MockResponse().setResponseCode(301).setHeader("Location",other.url("/me").toString().replace("localhost","127.0.0.1")))
            other.enqueue(json("""{"error":{"message":"Authentication credentials were not provided."}}""").setResponseCode(401))
            OkHttpClient().newCall(Request.Builder().url(origin.url("/me")).header("Authorization","Bearer test-access").build()).execute().use{assertEquals(401,it.code)}
            assertNull(other.takeRequest().getHeader("Authorization"))
        } finally {origin.shutdown();other.shutdown()}
    }
    @Test fun reproducesOld301LoginPostBecomingGet() {
        val origin=MockWebServer();val other=MockWebServer();origin.start();other.start()
        try {
            origin.enqueue(MockResponse().setResponseCode(301).setHeader("Location",other.url("/login")));other.enqueue(MockResponse().setResponseCode(405))
            OkHttpClient().newCall(Request.Builder().url(origin.url("/login")).post("{}".toRequestBody("application/json".toMediaType())).build()).execute().close()
            assertEquals("GET",other.takeRequest().method)
        } finally {origin.shutdown();other.shutdown()}
    }
    @Test fun validationEnvelopeUsesActualFieldNames() {
        assertEquals(mapOf("photo" to "This field is required."),validationFields(mapOf("fields" to mapOf("photo" to listOf("This field is required.")),"non_field" to emptyList<String>())))
    }
}
