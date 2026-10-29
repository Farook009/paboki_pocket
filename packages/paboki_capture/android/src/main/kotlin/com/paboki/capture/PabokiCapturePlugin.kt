package com.paboki.capture

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import java.util.concurrent.Executors

class PabokiCapturePlugin :
    FlutterPlugin,
    ActivityAware,
    MethodCallHandler,
    PluginRegistry.ActivityResultListener {

    private lateinit var channel: MethodChannel
    private var activity: Activity? = null
    private var pendingPrepare: MethodChannel.Result? = null
    private val io = Executors.newSingleThreadExecutor()

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(binding.binaryMessenger, CHANNEL)
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "prepareCapture" -> prepareCapture(result)
            "hasCaptureSession" -> result.success(ScreenCaptureService.ready)
            "analyzeChart" -> analyze(result)
            else -> result.notImplemented()
        }
    }

    private fun prepareCapture(result: MethodChannel.Result) {
        if (ScreenCaptureService.ready) {
            result.success(true)
            return
        }
        val act = activity
        if (act == null) {
            result.error("no_activity", "Screen capture must be granted from the main app.", null)
            return
        }
        pendingPrepare = result
        val mgr = act.getSystemService(Activity.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        act.startActivityForResult(mgr.createScreenCaptureIntent(), REQUEST_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != REQUEST_CAPTURE) return false
        val reply = pendingPrepare
        pendingPrepare = null
        val act = activity
        if (act != null && resultCode == Activity.RESULT_OK && data != null) {
            ScreenCaptureService.start(act, resultCode, data)
            reply?.success(true)
        } else {
            reply?.success(false)
        }
        return true
    }

    private fun analyze(result: MethodChannel.Result) {
        io.execute {
            try {
                val service = ScreenCaptureService.instance
                if (service == null || !ScreenCaptureService.ready) {
                    post(result) {
                        it.error("no_capture", "Screen capture is not active.", null)
                    }
                    return@execute
                }
                val bitmap = service.captureBitmap()
                if (bitmap == null) {
                    post(result) { it.error("capture_failed", "Could not capture the screen.", null) }
                    return@execute
                }
                val signal = OpenCvCandleDetector.analyze(bitmap)
                bitmap.recycle()
                post(result) {
                    it.success(
                        hashMapOf(
                            "direction" to signal.direction,
                            "confidence" to signal.confidence,
                            "detail" to signal.detail,
                            "expiry" to signal.expiry,
                        ),
                    )
                }
            } catch (error: Exception) {
                post(result) { it.error("analyze_failed", error.message, null) }
            }
        }
    }

    private fun post(result: MethodChannel.Result, body: (MethodChannel.Result) -> Unit) {
        android.os.Handler(android.os.Looper.getMainLooper()).post { body(result) }
    }

    companion object {
        const val CHANNEL = "com.paboki.pocket/capture"
        const val REQUEST_CAPTURE = 7101
    }
}
