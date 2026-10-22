package com.example.gamemaps.car

import org.junit.Assert.*
import org.junit.Test

class CarEngineOwnerTest {
    @Test fun carThenPhoneUsesOneEngine() {
        var created = 0
        val owner = CarEngineOwner { created++; Any() }
        val carEngine = owner.getOrCreate()
        assertSame(carEngine, owner.getOrCreate())
        assertEquals(1, created)
    }

    @Test fun phoneThenCarUsesOneEngine() {
        val engine = Any()
        val owner = CarEngineOwner { engine }
        val phoneEngine = owner.getOrCreate()
        assertSame(engine, phoneEngine)
        assertSame(phoneEngine, owner.getOrCreate())
    }

    @Test fun failedCreationIsNotCached() {
        var attempts = 0
        val engine = Any()
        val owner = CarEngineOwner {
            if (++attempts == 1) error("synthetic creation failure")
            engine
        }
        assertThrows(IllegalStateException::class.java) { owner.getOrCreate() }
        assertSame(engine, owner.getOrCreate())
        assertSame(engine, owner.getOrCreate())
        assertEquals(2, attempts)
    }

    @Test fun recursiveCreationFailsInsteadOfCreatingTwoEngines() {
        lateinit var owner: CarEngineOwner<Any>
        var recursive = true
        owner = CarEngineOwner { if (recursive) owner.getOrCreate() else Any() }
        assertThrows(IllegalStateException::class.java) { owner.getOrCreate() }
        recursive = false
        assertSame(owner.getOrCreate(), owner.getOrCreate())
    }

    @Test fun stalePhoneDetachCannotDetachReplacement() {
        val hosts = CarHostRegistry()
        val oldPhone = Any()
        val newPhone = Any()
        assertEquals(CarHostSnapshot(0, false, false), hosts.snapshot)
        assertEquals(1L, hosts.attachPhone(oldPhone).revision)
        val replacement = hosts.attachPhone(newPhone)
        assertEquals(2L, replacement.revision)
        assertEquals(replacement, hosts.detachPhone(oldPhone))
        assertFalse(hosts.isPhoneOwner(oldPhone))
        assertTrue(hosts.isPhoneOwner(newPhone))
        assertTrue(hosts.snapshot.phoneAttached)
        assertEquals(CarHostSnapshot(3, false, false), hosts.detachPhone(newPhone))
    }

    @Test fun reconnectAndDuplicateAttachmentDoNotCreateExtraTransitions() {
        val hosts = CarHostRegistry()
        val firstCar = Any()
        val secondCar = Any()
        val first = hosts.attachCar(firstCar)
        assertEquals(first, hosts.attachCar(firstCar))
        val replacement = hosts.attachCar(secondCar)
        assertEquals(2L, replacement.revision)
        assertEquals(replacement, hosts.detachCar(firstCar))
        assertTrue(hosts.snapshot.carAttached)
        val detached = hosts.detachCar(secondCar)
        assertEquals(CarHostSnapshot(3, false, false), detached)
        assertEquals(detached, hosts.detachCar(secondCar))
    }
}
