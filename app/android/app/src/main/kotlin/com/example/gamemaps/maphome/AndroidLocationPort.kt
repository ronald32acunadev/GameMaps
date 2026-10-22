package com.example.gamemaps.maphome

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock

class AndroidLocationPort(private val activity: Activity) : LocationPort {
    companion object { const val PERMISSION_REQUEST = 18253 }
    private val manager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val handler = Handler(Looper.getMainLooper())

    override fun permission(): LocationPermission = when {
        activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> LocationPermission.PRECISE
        activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED -> LocationPermission.APPROXIMATE
        else -> LocationPermission.NONE
    }

    override fun requestPermission() {
        activity.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST)
    }

    private fun providers(): List<String> = listOfNotNull(
        LocationManager.NETWORK_PROVIDER,
        if (permission() == LocationPermission.PRECISE) LocationManager.GPS_PROVIDER else null
    ).filter { manager.allProviders.contains(it) && manager.isProviderEnabled(it) }

    override fun providersEnabled() = providers().isNotEmpty()

    @Suppress("DEPRECATION")
    override fun requestFix(onFix: (NativePoint) -> Unit, onUnavailable: () -> Unit): () -> Unit {
        val started = SystemClock.elapsedRealtimeNanos()
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (location.elapsedRealtimeNanos < started) return
                try { onFix(NativePoint(location.latitude, location.longitude)) }
                catch (_: IllegalArgumentException) { onUnavailable() }
            }
            override fun onProviderDisabled(provider: String) { if (!providersEnabled()) onUnavailable() }
            override fun onProviderEnabled(provider: String) = Unit
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        }
        try {
            providers().forEach { manager.requestLocationUpdates(it, 0L, 0f, listener, Looper.getMainLooper()) }
        } catch (error: RuntimeException) {
            manager.removeUpdates(listener)
            throw error
        }
        return { manager.removeUpdates(listener) }
    }

    override fun scheduleTimeout(delayMillis: Long, action: () -> Unit): () -> Unit {
        val runnable = Runnable(action)
        handler.postDelayed(runnable, delayMillis)
        return { handler.removeCallbacks(runnable) }
    }
}
