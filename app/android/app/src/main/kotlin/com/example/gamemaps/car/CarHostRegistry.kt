package com.example.gamemaps.car

data class CarHostSnapshot(
    val revision: Long,
    val phoneAttached: Boolean,
    val carAttached: Boolean,
) {
    fun toMap(): Map<String, Any> = mapOf(
        "revision" to revision,
        "phoneAttached" to phoneAttached,
        "carAttached" to carAttached,
    )
}

/** Identity guards prevent an old host's teardown from detaching its replacement. */
class CarHostRegistry {
    private var phone: Any? = null
    private var car: Any? = null
    @get:Synchronized
    var snapshot = CarHostSnapshot(0, false, false)
        private set

    @Synchronized fun isPhoneOwner(owner: Any): Boolean = phone === owner

    @Synchronized fun attachPhone(owner: Any): CarHostSnapshot {
        if (phone !== owner) { phone = owner; update() }
        return snapshot
    }

    @Synchronized fun detachPhone(owner: Any): CarHostSnapshot {
        if (phone === owner) { phone = null; update() }
        return snapshot
    }

    @Synchronized fun attachCar(owner: Any): CarHostSnapshot {
        if (car !== owner) { car = owner; update() }
        return snapshot
    }

    @Synchronized fun detachCar(owner: Any): CarHostSnapshot {
        if (car === owner) { car = null; update() }
        return snapshot
    }

    private fun update() {
        snapshot = CarHostSnapshot(snapshot.revision + 1, phone != null, car != null)
    }
}
