package com.example.gamemaps.maphome

enum class LocationPermission { NONE, APPROXIMATE, PRECISE }

interface LocationPort {
    fun permission(): LocationPermission
    fun requestPermission()
    fun providersEnabled(): Boolean
    fun requestFix(onFix: (NativePoint) -> Unit, onUnavailable: () -> Unit): () -> Unit
    fun scheduleTimeout(delayMillis: Long, action: () -> Unit): () -> Unit
}

/** One foreground fix. Permission dialogs may pause the host without cancelling consent. */
class ForegroundLocationProvider(private val port: LocationPort) {
    private var pending: ((LocationOutcome) -> Unit)? = null
    private var generation = 0
    private var waitingPermission = false
    private var foreground = false
    private var disposed = false
    private var stopFix: (() -> Unit)? = null
    private var stopTimer: (() -> Unit)? = null

    fun locate(requestPermission: Boolean, result: (LocationOutcome) -> Unit) {
        if (disposed || pending != null) { result(LocationOutcome("unavailable")); return }
        pending = result
        generation++
        try {
            if (port.permission() == LocationPermission.NONE) {
                if (!requestPermission) { finish(LocationOutcome("denied")); return }
                waitingPermission = true
                port.requestPermission()
            } else if (foreground) acquire()
        } catch (_: RuntimeException) { finish(LocationOutcome("unavailable")) }
    }

    fun onPermissionResult() {
        if (pending == null || !waitingPermission) return
        waitingPermission = false
        if (port.permission() == LocationPermission.NONE) finish(LocationOutcome("denied"))
        else if (foreground) acquire()
    }

    fun onResume() {
        foreground = true
        if (pending != null && !waitingPermission && stopFix == null) acquire()
    }

    fun onPause() {
        foreground = false
        if (!waitingPermission) finish(LocationOutcome("unavailable"))
    }

    private fun acquire() {
        if (pending == null) return
        val request = generation
        try {
            if (port.permission() == LocationPermission.NONE) { finish(LocationOutcome("denied")); return }
            if (!port.providersEnabled()) { finish(LocationOutcome("disabled")); return }
            val cancel = port.requestFix({ point ->
                if (generation == request && pending != null) {
                    val permission = port.permission()
                    if (permission == LocationPermission.NONE) finish(LocationOutcome("denied"))
                    else finish(LocationOutcome("available", point, permission == LocationPermission.APPROXIMATE))
                }
            }, {
                if (generation == request && pending != null) finish(LocationOutcome("unavailable"))
            })
            if (generation != request || pending == null) { cancel(); return }
            stopFix = cancel
            val timer = port.scheduleTimeout(15000L) {
                if (generation == request && pending != null) finish(LocationOutcome("unavailable"))
            }
            if (generation != request || pending == null) timer() else stopTimer = timer
        } catch (_: RuntimeException) { finish(LocationOutcome("unavailable")) }
    }

    private fun finish(outcome: LocationOutcome) {
        val result = pending ?: return
        pending = null
        generation++
        waitingPermission = false
        val cancelFix = stopFix
        val cancelTimer = stopTimer
        stopFix = null
        stopTimer = null
        try { cancelFix?.invoke() } catch (_: RuntimeException) { /* Already removed. */ }
        cancelTimer?.invoke()
        result(outcome)
    }

    fun dispose() { disposed = true; foreground = false; finish(LocationOutcome("unavailable")) }
}
