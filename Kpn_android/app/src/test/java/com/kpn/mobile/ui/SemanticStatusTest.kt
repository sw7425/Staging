package com.kpn.mobile.ui
import org.junit.Assert.assertEquals
import org.junit.Test
class SemanticStatusTest {
 @Test fun successfulStatesShareSemanticToken() { listOf("VERIFIED","APPROVED","ONGOING","OPEN").forEach { assertEquals(StatusTone.SUCCESS,statusTone(it)) } }
 @Test fun reviewAndEscalationUseWarning() { listOf("PENDING","UNDER_REVIEW","FLAGGED","ESCALATED").forEach { assertEquals(StatusTone.WARNING,statusTone(it)) } }
 @Test fun adverseStatesUseDanger() { listOf("REJECTED","SUSPENDED","DISMISSED").forEach { assertEquals(StatusTone.DANGER,statusTone(it)) } }
 @Test fun unknownStatesStayNeutralAndCaseIsNormalized() { assertEquals(StatusTone.NEUTRAL,statusTone("future_state"));assertEquals(StatusTone.INFO,statusTone("planned")) }
}
