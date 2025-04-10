package cd.qrscanner.scanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.camera.core.CameraSelector
import cd.qrscanner.config.CameraConfig
import cd.qrscanner.utils.ICamera
import cd.qrscanner.analyzer.AnalyzeResult
import cd.qrscanner.analyzer.Analyzer

abstract class CameraScan<T> : ICamera {
    protected var isNeedTouchZoom = true
        private set
    protected var mExtras: Bundle? = null
    fun setNeedTouchZoom(needTouchZoom: Boolean): CameraScan<*> {
        isNeedTouchZoom = needTouchZoom
        return this
    }

    val extras: Bundle
        get() {
            if (mExtras == null) {
                mExtras = Bundle()
            }
            return mExtras!!
        }

    abstract fun setCameraConfig(cameraConfig: CameraConfig?): CameraScan<*>?
    abstract fun setAnalyzeImage(analyze: Boolean): CameraScan<*>?
    abstract fun setAnalyzer(analyzer: Analyzer<T>?): CameraScan<*>?
    abstract fun setVibrate(vibrate: Boolean): CameraScan<*>?
    abstract fun setPlayBeep(playBeep: Boolean): CameraScan<*>?
    abstract fun setOnScanResultCallback(callback: OnScanResultCallback<List<String>>): CameraScan<*>?
    abstract fun bindFlashlightView(v: View?,enableSensor : Boolean = true): CameraScan<*>?
    abstract fun setDarkLightLux(lightLux: Float): CameraScan<*>?
    abstract fun setBrightLightLux(lightLux: Float): CameraScan<*>?

    interface OnScanResultCallback<T> {
        fun onScanResultCallback(result: AnalyzeResult<T>)
        fun onScanResultFailure() {}
    }

    companion object {
        var SCAN_RESULT = "SCAN_RESULT"
        var LENS_FACING_FRONT = CameraSelector.LENS_FACING_FRONT
        var LENS_FACING_BACK = CameraSelector.LENS_FACING_BACK
        const val ASPECT_RATIO_4_3 = 4.0f / 3.0f
        const val ASPECT_RATIO_16_9 = 16.0f / 9.0f
        fun parseScanResult(data: Intent?): String? {
            return data?.getStringExtra(SCAN_RESULT)
        }
    }
}
