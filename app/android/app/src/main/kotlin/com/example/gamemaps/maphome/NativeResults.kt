package com.example.gamemaps.maphome

data class NativePoint(val latitude: Double, val longitude: Double) {
    init { require(latitude.isFinite() && longitude.isFinite() && latitude in -90.0..90.0 && longitude in -180.0..180.0) }
}

data class NativeDestination(val placeId: String, val name: String, val address: String?, val point: NativePoint) {
    init { require(placeId.isNotBlank() && name.isNotBlank()) }
}

data class LocationOutcome(val status: String, val point: NativePoint? = null, val approximate: Boolean = false) {
    fun payload(): Map<String, Any?> = if (point == null) mapOf("status" to status) else mapOf(
        "status" to status, "latitude" to point.latitude, "longitude" to point.longitude, "approximate" to approximate)
}

data class SearchOutcome(val status: String, val destination: NativeDestination? = null, val category: String? = null) {
    fun payload(): Map<String, Any?> = destination?.let { mapOf(
        "status" to status, "placeId" to it.placeId, "name" to it.name,
        "address" to it.address, "latitude" to it.point.latitude, "longitude" to it.point.longitude)
    } ?: mapOf("status" to status, "category" to category)
}
