package com.example.gamemaps.maphome

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesStatusCodes
import com.google.android.libraries.places.widget.PlaceAutocomplete

class AndroidPlacesPort(private val activity: Activity) : PlacesPort {
    companion object {
        const val SEARCH_REQUEST = 18254
        val DETAIL_FIELDS = listOf(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.LOCATION)
    }
    private val handler = Handler(Looper.getMainLooper())
    private var activityResult: ((SearchActivityOutcome) -> Unit)? = null

    override fun newSession(): Any = AutocompleteSessionToken.newInstance()

    @Suppress("DEPRECATION")
    override fun openSearch(bias: NativePoint?, token: Any, result: (SearchActivityOutcome) -> Unit) {
        val info = activity.packageManager.getApplicationInfo(activity.packageName, PackageManager.GET_META_DATA)
        val key = info.metaData?.getString("com.google.android.geo.API_KEY")
        require(!key.isNullOrBlank()) { "Places credential is unavailable" }
        if (!Places.isInitialized()) Places.initializeWithNewPlacesApiEnabled(activity.applicationContext, key)
        val builder = PlaceAutocomplete.IntentBuilder().setAutocompleteSessionToken(token as AutocompleteSessionToken)
        if (bias != null) {
            builder.setLocationBias(RectangularBounds.newInstance(
                LatLng((bias.latitude - 0.1).coerceAtLeast(-90.0), (bias.longitude - 0.1).coerceAtLeast(-180.0)),
                LatLng((bias.latitude + 0.1).coerceAtMost(90.0), (bias.longitude + 0.1).coerceAtMost(180.0))))
        }
        val intent = builder.build(activity)
        activityResult = result
        try { activity.startActivityForResult(intent, SEARCH_REQUEST) }
        catch (error: RuntimeException) { activityResult = null; throw error }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        if (requestCode != SEARCH_REQUEST) return false
        val result = activityResult ?: return true
        activityResult = null
        val status = intent?.let { PlaceAutocomplete.getResultStatusFromIntent(it) }
        if (status != null && !status.isSuccess && status.statusCode != CommonStatusCodes.CANCELED) {
            result(SearchActivityOutcome.Failed(category(status.statusCode)))
        } else if (resultCode == Activity.RESULT_CANCELED) {
            result(SearchActivityOutcome.Cancelled)
        } else if (resultCode != Activity.RESULT_OK || intent == null) {
            result(SearchActivityOutcome.Failed("resolution"))
        } else {
            val prediction = PlaceAutocomplete.getPredictionFromIntent(intent)
            val token = PlaceAutocomplete.getSessionTokenFromIntent(intent)
            result(SearchActivityOutcome.Selected(prediction?.placeId ?: "", token))
        }
        return true
    }

    override fun fetchPlace(id: String, token: Any, result: (NativeDestination?, String?) -> Unit) {
        val request = FetchPlaceRequest.builder(id, DETAIL_FIELDS)
            .setSessionToken(token as AutocompleteSessionToken).build()
        Places.createClient(activity.applicationContext).fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                val point = place.location
                try {
                    require(point != null)
                    result(NativeDestination(place.id ?: id, place.displayName ?: "", place.formattedAddress,
                        NativePoint(point.latitude, point.longitude)), null)
                } catch (_: IllegalArgumentException) { result(null, "resolution") }
            }
            .addOnFailureListener { error -> result(null, category((error as? ApiException)?.statusCode)) }
    }

    private fun category(code: Int?): String = when (code) {
        CommonStatusCodes.NETWORK_ERROR, CommonStatusCodes.TIMEOUT -> "network"
        CommonStatusCodes.DEVELOPER_ERROR, PlacesStatusCodes.REQUEST_DENIED -> "credentials"
        PlacesStatusCodes.OVER_QUERY_LIMIT -> "quota"
        PlacesStatusCodes.NOT_FOUND, PlacesStatusCodes.INVALID_REQUEST -> "resolution"
        else -> "unavailable"
    }

    override fun scheduleTimeout(delayMillis: Long, action: () -> Unit): () -> Unit {
        val runnable = Runnable(action)
        handler.postDelayed(runnable, delayMillis)
        return { handler.removeCallbacks(runnable) }
    }
    fun dispose() { activityResult = null }
}
