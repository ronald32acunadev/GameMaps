package com.example.gamemaps.car

import android.os.Looper
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel

/** Engine-scoped bootstrap channel; deliberately exposes no routing commands. */
class CarNavigationBridge(messenger: BinaryMessenger) {
    private val hosts = CarHostRegistry()
    private val channel = MethodChannel(messenger, "gamemaps/car_navigation")
    val snapshot: CarHostSnapshot get() = hosts.snapshot
    var acknowledgedRevision: Long = -1
        private set
    var dartBootCount: Int = 0
        private set

    init {
        channel.setMethodCallHandler { call, result ->
            checkMainThread()
            when (call.method) {
                "getHostState" -> result.success(snapshot.toMap())
                "ackHostState" -> {
                    val revision = call.argument<Number>("revision")?.toLong()
                    if (revision == null || revision < 0 || revision > snapshot.revision) {
                        result.error("protocol", "Invalid host revision.", null)
                    } else {
                        acknowledgedRevision = maxOf(acknowledgedRevision, revision)
                        result.success(null)
                    }
                }
                "bootstrapReady" -> {
                    dartBootCount++
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }
    }

    fun attachPhone(owner: Any) = publish { hosts.attachPhone(owner) }
    fun isPhoneOwner(owner: Any): Boolean = hosts.isPhoneOwner(owner)
    fun detachPhone(owner: Any) = publish { hosts.detachPhone(owner) }
    fun attachCar(owner: Any) = publish { hosts.attachCar(owner) }
    fun detachCar(owner: Any) = publish { hosts.detachCar(owner) }

    private fun publish(update: () -> CarHostSnapshot) {
        checkMainThread()
        val previous = snapshot
        val next = update()
        if (next != previous) channel.invokeMethod("hostChanged", next.toMap())
    }

    private fun checkMainThread() {
        check(Looper.myLooper() == Looper.getMainLooper()) { "Bootstrap requires the main thread." }
    }
}
