package com.kpn.mobile.core
import org.junit.Test
import org.junit.Assert.*
class KpnDatesTest {
 @Test fun utcNotificationTimesAreRenderedInNigeria(){assertEquals("7 Oct 2026 · 20:30",KpnDates.dateTime("2026-10-07T19:30:00Z"))}
 @Test fun publicationDayUsesNigeriaAcrossUtcMidnight(){assertEquals("8 Oct 2026",KpnDates.date("2026-10-07T23:30:00Z"))}
 @Test fun dateOnlyDeadlinesDoNotShift(){assertEquals("7 Oct 2026",KpnDates.date("2026-10-07"))}
 @Test fun unknownValuesAreNotInvented(){assertEquals("Unknown schedule",KpnDates.dateTime("Unknown schedule"));assertEquals("",KpnDates.date(null))}
}
