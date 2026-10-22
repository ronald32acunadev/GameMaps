package com.example.gamemaps

import android.content.Intent
import com.example.gamemaps.maphome.MapHomeBridge
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity : FlutterActivity() {
    private var mapHome: MapHomeBridge? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        mapHome = MapHomeBridge(this, flutterEngine.dartExecutor.binaryMessenger)
    }
    override fun onResume() { super.onResume(); mapHome?.onResume() }
    override fun onPause() { mapHome?.onPause(); super.onPause() }
    override fun onDestroy() { mapHome?.dispose(); mapHome = null; super.onDestroy() }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mapHome?.onPermissionResult(requestCode)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mapHome?.onActivityResult(requestCode, resultCode, data)
    }
}
