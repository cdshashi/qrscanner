package cd.qrscanner.utils

import androidx.annotation.FloatRange
import androidx.camera.core.Camera

interface ICamera {
    fun startCamera()
    fun stopCamera()
    val camera: Camera?
    fun release()
    fun zoomIn()
    fun zoomOut()
    fun zoomTo(ratio: Float)
    fun lineZoomIn()
    fun lineZoomOut()
    fun lineZoomTo(@FloatRange(from = 0.0, to = 1.0) linearZoom: Float)
    fun enableTorch(torch: Boolean)
    val isTorchEnabled: Boolean
    fun hasFlashUnit(): Boolean
}