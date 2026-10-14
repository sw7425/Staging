package com.kpn.mobile.ui
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class PublicCopyTest {
 private fun copy(key:String)=parseCanonicalCopy(File("src/main/assets/kpn_public_copy/$key.json").readText())
 @Test fun legalCopyIncludesAllFiveSections(){val text=copy("conduct").blocks.joinToString(" "){it.text};(1..5).forEach{assertTrue(text.contains("Section $it:"))};assertTrue(text.contains("14 days"));assertTrue(text.contains("Zero Tolerance Offenses"))}
 @Test fun faqRetainsFullWebsiteQuestionsAndAnswers(){val p=copy("faq");assertEquals(9,p.questions.size);assertEquals(setOf("about","membership","leadership","reporting"),p.questions.map{it.category}.toSet());assertTrue(p.questions.all{it.answer.isNotEmpty()})}
 @Test fun aboutAndHomePreserveCanonicalIdentity(){assertTrue(copy("about").blocks.any{it.text=="Our Network Structure"});assertEquals("Kebbi Progressive Youth Network",copy("home").blocks.first().text);assertTrue(copy("home").blocks.any{it.text=="Ready to Make a Difference?"})}
 @Test fun supportDoesNotBundleBankValues(){val p=copy("support");assertTrue(p.blocks.any{it.text=="Important Information"});assertFalse(p.blocks.any{it.text.contains("Access Bank")});assertFalse(p.blocks.any{it.text=="Account Number"})}
}
