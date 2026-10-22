package com.example.gamemaps.car

import android.Manifest
import android.app.Activity
import android.app.Instrumentation
import android.app.KeyguardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import com.example.gamemaps.MainActivity
import io.flutter.plugin.common.MethodChannel
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Explicit debug setup check; has no destination, guidance or simulation API. */
class NavigationSetupProbe : Instrumentation() {
    private var allowed = false

    override fun onCreate(arguments: Bundle?) {
        super.onCreate(arguments)
        allowed = arguments?.getString("allow_setup") == "true"
        start()
    }

    override fun onStart() {
        val output = Bundle()
        var activity: Activity? = null
        var stage = "scope"
        try {
            check(allowed && targetContext.packageName == "com.example.gamemaps")
            val lock = targetContext.getSystemService(KeyguardManager::class.java)
            if (lock.isKeyguardLocked) {
                output.putString("setup", "unlock_phone")
            } else {
                stage = "phone-start"
                val phone = startActivitySync(Intent(targetContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                activity = phone
                val app = targetContext.applicationContext as GameMapsApplication
                check(awaitCondition(30_000) {
                    app.requireBridge().dartBootCount == 1 && app.requireBridge().snapshot.phoneAttached
                })
                stage = "location-permission"
                val granted = awaitCondition(60_000) {
                    phone.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        phone.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                }
                if (!granted) {
                    output.putString("setup", "permission_missing")
                } else {
                    stage = "terms-and-session"
                    val completed = CountDownLatch(1)
                    runOnMainSync {
                        MethodChannel(app.requireEngine().dartExecutor.binaryMessenger, "gamemaps/navigation_setup_probe")
                            .invokeMethod("runSetup", null, object : MethodChannel.Result {
                                override fun success(value: Any?) {
                                    val report = value as? Map<*, *>
                                    val status = report?.get("status") as? String
                                    val allowedStatuses = setOf("ready", "needs_phone", "terms_declined", "not_authorized", "permission_missing", "terms_required", "unavailable", "cleanup_failed")
                                    output.putString("setup", if (status in allowedStatuses) status else "protocol")
                                    output.putBoolean("cleanup", report?.get("cleanup") == true)
                                    completed.countDown()
                                }
                                override fun error(code: String, message: String?, details: Any?) {
                                    output.putString("setup", "channel_error"); completed.countDown()
                                }
                                override fun notImplemented() {
                                    output.putString("setup", "channel_missing"); completed.countDown()
                                }
                            })
                    }
                    if (!completed.await(150, TimeUnit.SECONDS)) output.putString("setup", "timeout")
                }
            }
        } catch (_: Throwable) {
            output.putString("setup", "probe_failed")
            output.putString("failed_step", stage)
        } finally {
            output.putInt("destinationRequests", 0)
            activity?.let { owned -> runOnMainSync { owned.finish() } }
            finish(Activity.RESULT_OK, output)
        }
    }

    private fun awaitCondition(timeoutMs: Long, predicate: () -> Boolean): Boolean {
        val deadline = SystemClock.elapsedRealtime() + timeoutMs
        while (SystemClock.elapsedRealtime() < deadline) {
            var ready = false
            runOnMainSync { ready = predicate() }
            if (ready) return true
            SystemClock.sleep(50)
        }
        return false
    }
}
