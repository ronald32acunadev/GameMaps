package com.example.gamemaps.maphome

import org.junit.Assert.*
import org.junit.Test

class PlacesSearchProviderTest {
    private class Port : PlacesPort {
        val session = Any()
        var launches = 0
        var fetches = 0
        var usedSession: Any? = null
        var selected: ((SearchActivityOutcome) -> Unit)? = null
        var details: ((NativeDestination?, String?) -> Unit)? = null
        var timeout: (() -> Unit)? = null
        override fun newSession(): Any = session
        override fun openSearch(bias: NativePoint?, token: Any, result: (SearchActivityOutcome) -> Unit) {
            launches++
            assertSame(session, token)
            selected = result
        }
        override fun fetchPlace(id: String, token: Any, result: (NativeDestination?, String?) -> Unit) {
            fetches++
            usedSession = token
            details = result
        }
        override fun scheduleTimeout(delayMillis: Long, action: () -> Unit): () -> Unit {
            timeout = action
            return {}
        }
    }
    private fun destination() = NativeDestination("public", "Public place", null, NativePoint(9.9, -84.1))

    @Test fun selectedPlaceUsesOriginalSessionAndCompletesExactlyOnce() {
        val port = Port()
        val provider = PlacesSearchProvider(port)
        val results = mutableListOf<SearchOutcome>()
        provider.search(null, results::add)
        port.selected!!(SearchActivityOutcome.Selected("public", port.session))
        assertSame(port.session, port.usedSession)
        port.details!!(destination(), null)
        port.details!!(destination(), null)
        port.timeout!!()
        assertEquals(1, results.size)
        assertEquals("selected", results.single().status)
        assertEquals("public", results.single().destination!!.placeId)
    }

    @Test fun cancellationDoesNotFetchDetailsAndAllowsAnotherSearch() {
        val port = Port()
        val provider = PlacesSearchProvider(port)
        val results = mutableListOf<SearchOutcome>()
        provider.search(null, results::add)
        port.selected!!(SearchActivityOutcome.Cancelled)
        assertEquals("cancelled", results.single().status)
        assertEquals(0, port.fetches)
        provider.search(null, results::add)
        assertEquals(2, port.launches)
    }

    @Test fun duplicateSearchAndLateResultCannotReplaceActiveRequest() {
        val port = Port()
        val provider = PlacesSearchProvider(port)
        val first = mutableListOf<SearchOutcome>()
        val second = mutableListOf<SearchOutcome>()
        provider.search(null, first::add)
        provider.search(null, second::add)
        assertEquals(1, port.launches)
        assertEquals("failed", second.single().status)
        provider.dispose()
        port.selected!!(SearchActivityOutcome.Selected("public", port.session))
        assertEquals(0, port.fetches)
        assertEquals("cancelled", first.single().status)
    }

    @Test fun detailFailureAndTimeoutAreSanitizedFailures() {
        for (category in listOf("credentials", "quota", "network")) {
            val port = Port()
            val provider = PlacesSearchProvider(port)
            val results = mutableListOf<SearchOutcome>()
            provider.search(null, results::add)
            port.selected!!(SearchActivityOutcome.Selected("public", port.session))
            port.details!!(null, category)
            assertEquals(category, results.single().category)
            assertNull(results.single().destination)
        }
        val port = Port()
        val provider = PlacesSearchProvider(port)
        val results = mutableListOf<SearchOutcome>()
        provider.search(null, results::add)
        port.selected!!(SearchActivityOutcome.Selected("public", port.session))
        port.timeout!!()
        assertEquals("failed", results.single().status)
        port.details!!(destination(), null)
        assertEquals(1, results.size)
    }

    @Test fun nativeDetailsRequestOnlyIdentityDisplayAddressAndCoordinates() {
        assertEquals(listOf("ID", "DISPLAY_NAME", "FORMATTED_ADDRESS", "LOCATION"),
            AndroidPlacesPort.DETAIL_FIELDS.map { it.name })
    }

    @Test fun mismatchedSessionDoesNotFetchOrReturnADestination() {
        val port = Port()
        val provider = PlacesSearchProvider(port)
        val results = mutableListOf<SearchOutcome>()
        provider.search(null, results::add)
        port.selected!!(SearchActivityOutcome.Selected("public", Any()))
        assertEquals("resolution", results.single().category)
        assertNull(results.single().destination)
        assertEquals(0, port.fetches)
    }

    @Test fun invalidCoordinatesOrPlaceIdentityCannotBecomeDestination() {
        assertThrows(IllegalArgumentException::class.java) { NativePoint(Double.NaN, -84.0) }
        assertThrows(IllegalArgumentException::class.java) { NativePoint(91.0, -84.0) }
        assertThrows(IllegalArgumentException::class.java) { NativeDestination("", "Name", null, NativePoint(0.0, 0.0)) }
        val port = Port()
        val provider = PlacesSearchProvider(port)
        val results = mutableListOf<SearchOutcome>()
        provider.search(null, results::add)
        port.selected!!(SearchActivityOutcome.Selected("", port.session))
        assertEquals("resolution", results.single().category)
        assertEquals(0, port.fetches)
    }
}
