package com.kpn.mobile.core
import org.junit.Test
import org.junit.Assert.*
class InlineEditingTest {
 @Test fun selectedTextIsWrappedWithoutRemovingNeighbours(){assertEquals("One <b>KPN</b> network",wrapInline("One KPN network",4,7,"b").text)}
 @Test fun emptySelectionPlacesCursorInsideFormatting(){val edit=wrapInline("KPN",3,3,"i");assertEquals("KPN<i></i>",edit.text);assertEquals(6,edit.cursor)}
 @Test fun linkLabelAndUrlAreEscaped(){val edit=insertInlineLink("",0,0,"https://www.kpn.com.ng/?a=1&b=2","KPN & youth");assertTrue(edit.text.contains("&amp;"));assertFalse(edit.text.contains(" & "))}
 @Test(expected=IllegalArgumentException::class) fun unsafeLinkIsRejected(){insertInlineLink("",0,0,"javascript:alert(1)","Bad")}
 @Test fun simpleBodyAlsoUsesWebsiteCompatibleParagraphBlocks(){val payload=editorPayload("<b>Official story</b>",emptyList())!!;assertEquals("paragraph",((payload["blocks"] as List<*>).first() as Map<*,*>)["type"])}
}
