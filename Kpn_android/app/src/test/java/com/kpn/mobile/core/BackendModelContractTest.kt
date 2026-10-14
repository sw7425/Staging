package com.kpn.mobile.core
import com.kpn.mobile.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Test
class BackendModelContractTest {
    private val moshi=Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    @Test fun optionalIncidentTimeMayBeNull(){val report=moshi.adapter(CommunityReport::class.java).fromJson("""{"id":1,"incident_time":null,"category":"COMMUNITY"}""")!!;assertNull(report.incident_time);assertEquals("COMMUNITY",report.category)}
    @Test fun legacyArticleBlocksMayBeJsonString(){val article=moshi.adapter(Article::class.java).fromJson("""{"id":1,"content_json":"{\"blocks\":[]}"}""")!!;assertTrue(article.content_json is String)}
    @Test fun modernArticleBlocksRemainStructured(){val article=moshi.adapter(Article::class.java).fromJson("""{"id":1,"content_json":{"blocks":[{"type":"paragraph","data":{"text":"Official content"}}]}}""")!!;assertTrue(article.content_json is Map<*,*>)}
    @Test fun grandPatronUsesTheActualBackendChoice(){val patron=moshi.adapter(Patron::class.java).fromJson("""{"id":1,"patron_type":"GRAND","full_name":"QA fixture"}""")!!;assertTrue(patron.isGrandPatron);assertFalse(Patron(patron_type="PATRON").isGrandPatron)}
}
