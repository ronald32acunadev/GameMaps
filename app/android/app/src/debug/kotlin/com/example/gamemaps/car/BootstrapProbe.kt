package com.example.gamemaps.car

import android.app.Activity
import android.app.Instrumentation
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import io.flutter.embedding.engine.FlutterEngine

/** Explicit debug instrumentation, restricted to an isolated dummy-key package. */
class BootstrapProbe : Instrumentation() {
    override fun onCreate(arguments: Bundle?) {
        super.onCreate(arguments)
        start()
    }

    override fun onStart() {
        val output = Bundle()
        var step = "isolated-package"
        try {
            check(targetContext.packageName == "com.example.gamemaps.bootstrapprobe")
            @Suppress("DEPRECATION")
            val info = targetContext.packageManager.getApplicationInfo(
                targetContext.packageName, PackageManager.GET_META_DATA,
            )
            check(info.metaData.getString("com.google.android.geo.API_KEY")?.startsWith("dummy-") == true)
            val app = targetContext.applicationContext as GameMapsApplication
            var engine: FlutterEngine? = null
            step = "headless-engine"
            runOnMainSync {
                engine = app.requireEngine()
                check(engine === app.requireEngine())
                check(!app.requireBridge().snapshot.phoneAttached)
            }
            step = "dart-ready"
            awaitBridge(app) { it.dartBootCount == 1 && it.acknowledgedRevision == it.snapshot.revision }
            val firstCar = Any()
            val replacementCar = Any()
            step = "car-host-roundtrip"
            runOnMainSync { app.requireBridge().attachCar(firstCar) }
            awaitBridge(app) { it.snapshot.carAttached && it.acknowledgedRevision == it.snapshot.revision }
            step = "reconnect-roundtrip"
            runOnMainSync {
                app.requireBridge().attachCar(replacementCar)
                app.requireBridge().detachCar(firstCar)
                check(app.requireBridge().snapshot.carAttached)
                check(engine === app.requireEngine())
            }
            awaitBridge(app) { it.acknowledgedRevision == it.snapshot.revision }
            step = "disconnect-roundtrip"
            runOnMainSync { app.requireBridge().detachCar(replacementCar) }
            awaitBridge(app) {
                !it.snapshot.carAttached && !it.snapshot.phoneAttached &&
                    it.dartBootCount == 1 && it.acknowledgedRevision == it.snapshot.revision
            }
            output.putString("bootstrap", "pass")
            output.putString("scope", "Engine and channel only; navigation listeners and car surface unverified.")
            finish(Activity.RESULT_OK, output)
        } catch (_: Throwable) {
            output.putString("bootstrap", "failed")
            output.putString("failed_step", step)
            finish(Activity.RESULT_CANCELED, output)
        }
    }

    private fun awaitBridge(app: GameMapsApplication, predicate: (CarNavigationBridge) -> Boolean) {
        val deadline = SystemClock.elapsedRealtime() + 15000
        while (SystemClock.elapsedRealtime() < deadline) {
            var ready = false
            runOnMainSync { ready = predicate(app.requireBridge()) }
            if (ready) return
            SystemClock.sleep(20)
        }
        error("Bootstrap probe timed out.")
    }
}
