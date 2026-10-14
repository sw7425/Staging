package com.kpn.mobile.core

import com.kpn.mobile.data.CommunityOutreach
import com.kpn.mobile.data.Discipline
import com.kpn.mobile.data.KpnApi
import com.kpn.mobile.data.LegalOpinionRequest
import com.kpn.mobile.data.TelegramLink
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class BuildRepairContractTest {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Test fun telegramLinkResponseIncludesConnectionUrl() {
        val response = moshi.adapter(TelegramLink::class.java).fromJson(
            """{"url":"https://t.me/KPNKebbiBot?start=kpn_test","web_url":"https://kpn.com.ng/connect/","expires_at":"2026-10-07T04:00:00Z"}"""
        )!!
        assertEquals("https://t.me/KPNKebbiBot?start=kpn_test", response.url)
        assertNotNull(response.web_url)
        assertNotNull(response.expires_at)
    }

    @Test fun minimalTelegramResponseRemainsCompatible() {
        val response = moshi.adapter(TelegramLink::class.java)
            .fromJson("""{"url":"https://t.me/KPNKebbiBot?start=kpn_test"}""")!!
        assertNull(response.web_url)
        assertNull(response.expires_at)
    }

    @Test fun legalApprovalIsExplicitInRequestJson() {
        val json = moshi.adapter(LegalOpinionRequest::class.java)
            .toJson(LegalOpinionRequest("Reviewed evidence", legal_approved = true))
        assertTrue(json.contains("\"legal_approved\":true"))
        assertTrue(json.contains("\"legal_opinion\":\"Reviewed evidence\""))
    }

    @Test fun disciplineUsesExistingResponseModel() {
        val response = moshi.adapter(Discipline::class.java).fromJson(
            """{"id":7,"user":9,"action_type":"WARNING","reason":"Review requested","legal_opinion":"Reviewed","legal_approved":true}"""
        )!!
        assertEquals(7, response.id)
        assertTrue(response.legal_approved)
    }

    @Test fun outreachSubmissionFieldsRoundTrip() {
        val record = CommunityOutreach(
            organization = "Community association",
            contact_person = "Representative",
            purpose = "Consultation",
            date = "2026-10-07",
            location = "Kebbi",
        )
        val adapter = moshi.adapter(CommunityOutreach::class.java)
        assertEquals(record, adapter.fromJson(adapter.toJson(record)))
    }

    @Test fun everyRetrofitEndpointHasValidAnnotations() {
        val api = Retrofit.Builder()
            .baseUrl("https://example.invalid/api/v1/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .validateEagerly(true)
            .build()
            .create(KpnApi::class.java)
        assertNotNull(api)
    }
}