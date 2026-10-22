package com.example.gamemaps.maphome

sealed class SearchActivityOutcome {
    data object Cancelled : SearchActivityOutcome()
    data class Failed(val category: String) : SearchActivityOutcome()
    data class Selected(val placeId: String, val token: Any?) : SearchActivityOutcome()
}

interface PlacesPort {
    fun newSession(): Any
    fun openSearch(bias: NativePoint?, token: Any, result: (SearchActivityOutcome) -> Unit)
    fun fetchPlace(id: String, token: Any, result: (NativeDestination?, String?) -> Unit)
    fun scheduleTimeout(delayMillis: Long, action: () -> Unit): () -> Unit
}

/** Only an explicit search launches Places; one original session covers selection/details. */
class PlacesSearchProvider(private val port: PlacesPort) {
    private var pending: ((SearchOutcome) -> Unit)? = null
    private var generation = 0
    private var disposed = false
    private var stopTimer: (() -> Unit)? = null
    private var resolving = false

    fun search(bias: NativePoint?, result: (SearchOutcome) -> Unit) {
        if (disposed || pending != null) { result(SearchOutcome("failed", category = "unavailable")); return }
        pending = result
        val request = ++generation
        resolving = false
        try {
            val session = port.newSession()
            port.openSearch(bias, session) { activity ->
                if (generation != request || pending == null || resolving) return@openSearch
                when (activity) {
                    SearchActivityOutcome.Cancelled -> finish(SearchOutcome("cancelled"))
                    is SearchActivityOutcome.Failed -> finish(SearchOutcome("failed", category = safeCategory(activity.category)))
                    is SearchActivityOutcome.Selected -> {
                        if (activity.placeId.isBlank() || (activity.token != null && activity.token != session)) {
                            finish(SearchOutcome("failed", category = "resolution"))
                        } else {
                            resolving = true
                            try {
                                stopTimer = port.scheduleTimeout(30000L) {
                                    if (generation == request) finish(SearchOutcome("failed", category = "network"))
                                }
                                port.fetchPlace(activity.placeId, session) { place, error ->
                                    if (generation == request && pending != null) {
                                        if (place != null) finish(SearchOutcome("selected", place))
                                        else finish(SearchOutcome("failed", category = safeCategory(error)))
                                    }
                                }
                            } catch (_: RuntimeException) { finish(SearchOutcome("failed", category = "unavailable")) }
                        }
                    }
                }
            }
        } catch (_: RuntimeException) { finish(SearchOutcome("failed", category = "credentials")) }
    }

    private fun safeCategory(value: String?) = if (value in setOf("credentials", "quota", "network", "resolution")) value!! else "unavailable"

    private fun finish(value: SearchOutcome) {
        val result = pending ?: return
        pending = null
        generation++
        stopTimer?.invoke()
        stopTimer = null
        result(value)
    }

    fun dispose() { disposed = true; finish(SearchOutcome("cancelled")) }
}
