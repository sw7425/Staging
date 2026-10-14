package com.kpn.mobile.core

import com.kpn.mobile.BuildConfig
import com.kpn.mobile.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CancellationException
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed interface Result<out T> {
    data class Ok<T>(val value: T) : Result<T>
    data class Err(val code: String, val message: String, val status: Int? = null, val fields: Map<String, String> = emptyMap()) : Result<Nothing>
}

/** Persistence interface makes the actual production HTTP chain testable without Android mocks. */
interface TokenStore {
    fun access(): String?
    fun refresh(): String?
    fun save(access: String, refresh: String? = null)
    fun clearTokens()
    fun saveIfUnchanged(expectedAccess:String?,expectedRefresh:String,access:String,refresh:String?):Boolean=synchronized(this) {
        if(this.access()!=expectedAccess || this.refresh()!=expectedRefresh) false else { save(access,refresh);true }
    }
    fun clearIfUnchanged(expectedAccess:String?,expectedRefresh:String):Boolean=synchronized(this) {
        if(this.access()!=expectedAccess || this.refresh()!=expectedRefresh) false else { clearTokens();true }
    }
}

private val PUBLIC_PATHS = listOf("/auth/login/", "/auth/refresh/", "/auth/register/", "/auth/password-reset/", "/registration/vacant-roles/", "/newsroom/", "/opportunities/", "/community/", "/civic/", "/advocacy/", "/patrons/", "/leadership/", "/leadership-directory/", "/impact/", "/locations/", "/roles/", "/content/", "/gallery/")
internal fun isPublicPath(path: String) = PUBLIC_PATHS.any { path.startsWith(it) }
internal fun apiPath(path: String) = path.removePrefix("/api/v1")
internal fun validationFields(details: Any?): Map<String, String> {
    val wrapper = details as? Map<*, *> ?: return emptyMap()
    val source = (wrapper["fields"] as? Map<*, *>) ?: wrapper
    return source.mapNotNull { (k, v) ->
        val message = when (v) {
            is List<*> -> v.filterNotNull().joinToString(" ")
            is Map<*, *> -> v.values.filterNotNull().joinToString(" ")
            null -> null
            else -> v.toString()
        }?.trim()
        if (k == null || message.isNullOrBlank()) null else k.toString() to message
    }.toMap()
}

class TokenAuthenticator(private val store: TokenStore, private val refreshApi: KpnApi) : Authenticator {
    @Synchronized override fun authenticate(route: Route?, response: Response): Request? {
        if (response.priorResponse != null || isPublicPath(apiPath(response.request.url.encodedPath))) return null
        val snapshot=synchronized(store){store.access() to store.refresh()}
        val current = snapshot.first?.takeIf { it.isNotBlank() }
        // Another request may already have rotated the token while this request was in flight.
        if (current != null && response.request.header("Authorization") != "Bearer $current") {
            return response.request.newBuilder().header("Authorization", "Bearer $current").build()
        }
        val refresh = snapshot.second?.takeIf { it.isNotBlank() } ?: return null
        return try {
            val refreshed = refreshApi.refreshCall(RefreshRequest(refresh)).execute()
            if (!refreshed.isSuccessful) {
                if (refreshed.code() in listOf(400, 401, 403)) store.clearIfUnchanged(snapshot.first,refresh)
                return null
            }
            val pair = refreshed.body()?.takeIf { it.access.isNotBlank() } ?: return null
            if(!store.saveIfUnchanged(snapshot.first,refresh,pair.access,pair.refresh)) return null
            response.request.newBuilder().header("Authorization", "Bearer ${pair.access}").build()
        } catch (_: IOException) { null }
    }
}

class ApiClient(store: TokenStore, baseUrl: String = BuildConfig.API_BASE_URL) {
    val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val canonical = baseUrl.toHttpUrl()
    private val transport = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
        // Authentication is never forwarded through a host-changing redirect. Use the canonical host.
        .followRedirects(false).followSslRedirects(false).build()
    private fun retrofit(client: OkHttpClient) = Retrofit.Builder().baseUrl(canonical).client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi)).build().create(KpnApi::class.java)
    private val refreshApi = retrofit(transport)
    private val client = transport.newBuilder().addInterceptor { chain ->
        val request = chain.request()
        val builder = request.newBuilder().header("Accept", "application/json")
        if (request.url.host == canonical.host && request.url.port == canonical.port && !isPublicPath(apiPath(request.url.encodedPath))) {
            store.access()?.takeIf { it.isNotBlank() }?.let { builder.header("Authorization", "Bearer $it") }
        }
        chain.proceed(builder.build())
    }.authenticator(TokenAuthenticator(store, refreshApi)).build()
    val api: KpnApi = retrofit(client)
    suspend fun <T> safe(block: suspend () -> T): Result<T> = try {
        val value=block()
        if(value is retrofit2.Response<*> && !value.isSuccessful) throw HttpException(value)
        Result.Ok(value)
    } catch (e: CancellationException) { throw e
    } catch (e: HttpException) {
        val errorBody=e.response()?.errorBody()
        val body=runCatching{errorBody?.string()}.getOrNull()
        val missingEndpoint=e.code()==404 && (errorBody?.contentType()?.subtype=="html" || body?.trimStart()?.startsWith("<!doctype html>",ignoreCase=true)==true)
        val envelope = try { body?.let { moshi.adapter(ApiErrorEnvelope::class.java).fromJson(it) } } catch (_: Exception) { null }
        val fields = validationFields(envelope?.error?.details)
        val message = if (e.code() in 300..399) "The API redirected this request. Check the configured canonical HTTPS API address." else if(missingEndpoint)"This page’s API is not available on the deployed KPN server. The backend update is required." else envelope?.error?.message ?: fields.values.firstOrNull() ?: if(e.code()==404)"This item could not be found. It may have been removed or is outside your access." else "Request failed (${e.code()})"
        Result.Err(if(missingEndpoint)"endpoint_unavailable" else envelope?.error?.code ?: "http_error", message, e.code(), fields)
    } catch (_: IOException) { Result.Err("network_error", "Unable to reach KPN. Check your connection and try again.")
    } catch (e: Exception) { Result.Err("client_error", e.message ?: "Unable to read the server response.") }
}
