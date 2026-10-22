package com.example.gamemaps.maphome

import android.app.Activity
import android.content.Intent
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel

class MapHomeBridge(activity: Activity, messenger: BinaryMessenger) {
    private val location = ForegroundLocationProvider(AndroidLocationPort(activity))
    private val placesPort = AndroidPlacesPort(activity)
    private val places = PlacesSearchProvider(placesPort)
    private val channel = MethodChannel(messenger, "gamemaps/map_home")

    init {
        channel.setMethodCallHandler { call, result ->
            when (call.method) {
                "locate" -> location.locate(call.argument<Boolean>("requestPermission") == true) {
                    result.success(it.payload())
                }
                "search" -> {
                    try {
                        val latitude = call.argument<Number>("latitude")?.toDouble()
                        val longitude = call.argument<Number>("longitude")?.toDouble()
                        val bias = if (latitude == null || longitude == null) null else NativePoint(latitude, longitude)
                        places.search(bias) { result.success(it.payload()) }
                    } catch (_: RuntimeException) { result.success(SearchOutcome("failed", category = "resolution").payload()) }
                }
                else -> result.notImplemented()
            }
        }
    }
    fun onResume() = location.onResume()
    fun onPause() = location.onPause()
    fun onPermissionResult(requestCode: Int) {
        if (requestCode == AndroidLocationPort.PERMISSION_REQUEST) location.onPermissionResult()
    }
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = placesPort.onActivityResult(requestCode, resultCode, data)
    fun dispose(clearChannel: Boolean = true) {
        location.dispose()
        places.dispose()
        placesPort.dispose()
        if (clearChannel) channel.setMethodCallHandler(null)
    }
}
