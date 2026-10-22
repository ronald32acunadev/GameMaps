package com.example.gamemaps

import android.content.Intent
import android.content.Context
import com.example.gamemaps.car.GameMapsApplication
import com.example.gamemaps.maphome.MapHomeBridge
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity : FlutterActivity() {
    private var mapHome: MapHomeBridge? = null
    private val runtime: GameMapsApplication get() = application as GameMapsApplication

    override fun provideFlutterEngine(context: Context): FlutterEngine = runtime.requireEngine()
    override fun shouldDestroyEngineWithHost(): Boolean = false

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        mapHome = MapHomeBridge(this, flutterEngine.dartExecutor.binaryMessenger)
        runtime.requireBridge().attachPhone(this)
    }
    override fun onResume() { super.onResume(); mapHome?.onResume() }
    override fun onPause() { mapHome?.onPause(); super.onPause() }
    override fun onDestroy() {
        // An old Activity must not clear a replacement Activity's channel.
        mapHome?.dispose(clearChannel = runtime.requireBridge().isPhoneOwner(this))
        mapHome = null
        super.onDestroy()
        // Notify Dart after the plugin's Activity-detach callbacks have run.
        runtime.requireBridge().detachPhone(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mapHome?.onPermissionResult(requestCode)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mapHome?.onActivityResult(requestCode, resultCode, data)
    }
}
