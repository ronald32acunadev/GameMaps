package com.example.gamemaps.maphome

import org.junit.Assert.*
import org.junit.Test

class ForegroundLocationProviderTest {
    private class Port : LocationPort {
        var permission = LocationPermission.NONE
        var enabled = true
        var prompts = 0
        var stops = 0
        var timersCancelled = 0
        var requests = 0
        var fix: ((NativePoint) -> Unit)? = null
        var timeout: (() -> Unit)? = null
        override fun permission() = permission
        override fun requestPermission() { prompts++ }
        override fun providersEnabled() = enabled
        override fun requestFix(onFix: (NativePoint) -> Unit, onUnavailable: () -> Unit): () -> Unit {
            requests++
            fix = onFix
            return { stops++ }
        }
        override fun scheduleTimeout(delayMillis: Long, action: () -> Unit): () -> Unit {
            assertEquals(15000L, delayMillis)
            timeout = action
            return { timersCancelled++ }
        }
    }

    @Test fun approximatePermissionReturnsActualFixAndCleansUpOnce() {
        val port = Port().apply { permission = LocationPermission.APPROXIMATE }
        val provider = ForegroundLocationProvider(port)
        val results = mutableListOf<LocationOutcome>()
        provider.onResume()
        provider.locate(true, results::add)
        assertEquals(0, port.prompts)
        port.fix!!(NativePoint(9.9, -84.1))
        port.timeout!!()
        assertEquals(1, results.size)
        assertEquals("available", results.single().status)
        assertTrue(results.single().approximate)
        assertEquals(1, port.stops)
        assertEquals(1, port.timersCancelled)
    }

    @Test fun permissionDialogPauseDoesNotLoseRequestAndDenialDoesNotLoop() {
        val port = Port()
        val provider = ForegroundLocationProvider(port)
        val results = mutableListOf<LocationOutcome>()
        provider.onResume()
        provider.locate(true, results::add)
        provider.onPause()
        assertEquals(0, results.size)
        provider.onPermissionResult()
        provider.onResume()
        assertEquals("denied", results.single().status)
        assertEquals(1, port.prompts)
        assertEquals(0, port.requests)
    }

    @Test fun approximateGrantAfterDialogWaitsForForegroundBeforeAcquisition() {
        val port = Port()
        val provider = ForegroundLocationProvider(port)
        val results = mutableListOf<LocationOutcome>()
        provider.onResume()
        provider.locate(true, results::add)
        provider.onPause()
        port.permission = LocationPermission.APPROXIMATE
        provider.onPermissionResult()
        assertEquals(0, port.requests)
        provider.onResume()
        port.fix!!(NativePoint(9.9, -84.1))
        assertEquals("available", results.single().status)
        assertTrue(results.single().approximate)
    }

    @Test fun disabledProviderAndTimeoutNeverInventPosition() {
        val port = Port().apply { permission = LocationPermission.PRECISE; enabled = false }
        val provider = ForegroundLocationProvider(port)
        val results = mutableListOf<LocationOutcome>()
        provider.onResume()
        provider.locate(true, results::add)
        assertEquals("disabled", results.last().status)
        assertNull(results.last().point)
        port.enabled = true
        provider.locate(true, results::add)
        port.timeout!!()
        assertEquals("unavailable", results.last().status)
        assertNull(results.last().point)
        assertEquals(1, port.stops)
    }

    @Test fun lateFixFromPreviousRequestCannotCompleteNewRequestWithSameConsumer() {
        val port = Port().apply { permission = LocationPermission.PRECISE }
        val provider = ForegroundLocationProvider(port)
        val results = mutableListOf<LocationOutcome>()
        val consumer: (LocationOutcome) -> Unit = { results.add(it) }
        provider.onResume()
        provider.locate(true, consumer)
        val staleFix = port.fix!!
        provider.onPause()
        provider.onResume()
        provider.locate(true, consumer)
        staleFix(NativePoint(9.0, -84.0))
        assertEquals(1, results.size)
        port.fix!!(NativePoint(10.0, -85.0))
        assertEquals(2, results.size)
        assertEquals(NativePoint(10.0, -85.0), results.last().point)
    }

    @Test fun pauseAndDisposeStopUpdatesAndIgnoreLateCompletion() {
        val port = Port().apply { permission = LocationPermission.PRECISE }
        val provider = ForegroundLocationProvider(port)
        val results = mutableListOf<LocationOutcome>()
        provider.onResume()
        provider.locate(true, results::add)
        provider.onPause()
        port.fix!!(NativePoint(9.9, -84.1))
        provider.dispose()
        assertEquals(1, results.size)
        assertEquals("unavailable", results.single().status)
        assertEquals(1, port.stops)
    }
}
