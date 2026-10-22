package com.example.gamemaps.car

import android.app.Application
import android.os.Looper
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor

class GameMapsApplication : Application() {
    private var bridge: CarNavigationBridge? = null
    private val owner = CarEngineOwner {
        // FlutterEngine performs normal generated-plugin registration once.
        val engine = FlutterEngine(this)
        try {
            bridge = CarNavigationBridge(engine.dartExecutor.binaryMessenger)
            FlutterEngineCache.getInstance().put(ENGINE_ID, engine)
            engine.dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
            engine
        } catch (failure: Throwable) {
            bridge = null
            FlutterEngineCache.getInstance().remove(ENGINE_ID)
            engine.destroy()
            throw failure
        }
    }

    fun requireEngine(): FlutterEngine {
        check(Looper.myLooper() == Looper.getMainLooper()) { "Engine startup requires the main thread." }
        return owner.getOrCreate()
    }

    fun requireBridge(): CarNavigationBridge {
        requireEngine()
        return checkNotNull(bridge)
    }

    companion object {
        const val ENGINE_ID = "gamemaps-navigation"
    }
}
