package com.example.gamemaps.car

/** Own one runtime independently of the phone Activity or a car surface. */
class CarEngineOwner<T : Any>(private val create: () -> T) {
    private var engine: T? = null
    private var creating = false

    @Synchronized
    fun getOrCreate(): T {
        engine?.let { return it }
        check(!creating) { "Engine creation cannot be recursive." }
        creating = true
        try {
            return create().also { engine = it }
        } finally {
            creating = false
        }
    }
}
