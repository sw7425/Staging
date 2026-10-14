package com.kpn.mobile.core
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.MockResponse
import org.junit.Test
import org.junit.Assert.*
class EndpointFailureTest {
 @Test fun missingDeployedEndpointIsNotConfusedWithAuthentication()=runTest {
  val server=MockWebServer();server.start()
  try {
   server.enqueue(MockResponse().setResponseCode(404).setHeader("Content-Type","text/html").setBody("<!doctype html><title>Not Found</title>"))
   val c=ApiClient(RetirementTokens(null,null),server.url("/api/v1/").toString())
   val result=c.safe{c.api.homeContent()} as Result.Err
   assertEquals(404,result.status);assertEquals("endpoint_unavailable",result.code);assertTrue(result.message.contains("backend update"))
  } finally{server.shutdown()}
 }
 @Test fun missingRecordPreservesServerErrorInsteadOfClaimingMissingApi()=runTest {
  val server=MockWebServer();server.start()
  try {
   server.enqueue(MockResponse().setResponseCode(404).setHeader("Content-Type","application/json").setBody("{\"error\":{\"code\":\"not_found\",\"message\":\"The record is unavailable\",\"details\":null}}"))
   val c=ApiClient(RetirementTokens("access",null),server.url("/api/v1/").toString())
   val result=c.safe{c.api.publicPerson(999)} as Result.Err
   assertEquals(404,result.status);assertEquals("not_found",result.code);assertEquals("The record is unavailable",result.message)
  } finally{server.shutdown()}
 }
}
