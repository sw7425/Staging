package com.kpn.mobile.core
import com.kpn.mobile.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Test
class PhaseZeroContractTest{private val moshi=Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
@Test fun publicPaths(){listOf("/auth/login/","/newsroom/a/","/community/","/civic/","/advocacy/","/patrons/","/leadership/","/impact/").forEach{assertTrue(isPublicPath(it))};assertFalse(isPublicPath("/auth/me/"))}
@Test fun validation(){assertEquals("Required.",validationFields(mapOf("username" to listOf("Required.")))["username"])}
@Test fun identity(){val u=moshi.adapter(UserDto::class.java).fromJson("""{"id":1,"username":"p","status":"VERIFIED","role":"STATE","tier":"STATE","role_title":"President","access":{"state":"ACTIVE","telegram_required":true,"telegram_verified":true},"capabilities":["VIEW_MEMBERS","REVIEW_APPLICANTS","VIEW_FINANCE"]}""")!!;val p=RolePolicies.fromUser(u);assertEquals(Tier.STATE,p.tier);assertTrue(Capability.FINANCE in p.capabilities)}
@Test fun profileSeparate(){assertEquals("Hassan",moshi.adapter(ProfileDto::class.java).fromJson("""{"first_name":"Hassan"}""")!!.first_name)}
@Test fun dashboardBlocks(){val d=moshi.adapter(DashboardEnvelope::class.java).fromJson("""{"title":"Executive","blocks":[{"key":"finance","type":"finance","title":"Finance","items":[{"key":"balance","label":"Balance","value":"10","currency":"NGN"}]}]}""")!!;assertEquals("finance",d.blocks.single().key)}
@Test fun safeDeepLinks(){assertEquals("article/story",DeepLinkParser.parse(null,"/newsroom/story/",null,"1").route);assertEquals("notifications",DeepLinkParser.parse("https://evil.test",null,null,null).route)}}
