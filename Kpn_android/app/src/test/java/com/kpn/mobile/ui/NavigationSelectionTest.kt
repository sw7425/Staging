package com.kpn.mobile.ui
import org.junit.Assert.*
import org.junit.Test
class NavigationSelectionTest {
 @Test fun articleHighlightsOnlyNews(){assertTrue(publicTabSelected("article/{slug}","news"));listOf("home","opportunities","more_public").forEach{assertFalse(publicTabSelected("article/{slug}",it))}}
 @Test fun rootsAndMissingRouteRemainCorrect(){assertTrue(publicTabSelected("home","home"));assertFalse(publicTabSelected(null,"news"));assertFalse(publicTabSelected("leadership","news"))}
}
