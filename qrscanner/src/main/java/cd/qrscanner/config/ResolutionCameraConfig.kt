package cd.qrscanner.config

import android.content.Context
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import cd.qrscanner.utils.LogUtils
import cd.qrscanner.scanner.CameraScan
import java.util.*

open class ResolutionCameraConfig @JvmOverloads constructor(
    context: Context,
    imageQuality: Int = IMAGE_QUALITY_1080P
) : CameraConfig() {
    private var mTargetSize: Size? = null

    init {
        initTargetResolutionSize(context, imageQuality)
    }

    private fun initTargetResolutionSize(context: Context, imageQuality: Int) {
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        LogUtils.d(String.format(Locale.getDefault(), "displayMetrics: %dx%d", width, height))
        mTargetSize = if (width < height) {
            val ratio = height / width.toFloat()
            val size = Math.min(width, imageQuality)
            if (Math.abs(ratio - CameraScan.ASPECT_RATIO_4_3) < Math.abs(ratio - CameraScan.ASPECT_RATIO_16_9)) {
                Size(size, Math.round(size * CameraScan.ASPECT_RATIO_4_3))
            } else {
                Size(size, Math.round(size * CameraScan.ASPECT_RATIO_16_9))
            }
        } else {
            val size = Math.min(height, imageQuality)
            val ratio = width / height.toFloat()
            if (Math.abs(ratio - CameraScan.ASPECT_RATIO_4_3) < Math.abs(ratio - CameraScan.ASPECT_RATIO_16_9)) {
                Size(Math.round(size * CameraScan.ASPECT_RATIO_4_3), size)
            } else {
                Size(Math.round(size * CameraScan.ASPECT_RATIO_16_9), size)
            }
        }
        LogUtils.d("targetSize: $mTargetSize")
    }

    override fun options(builder: CameraSelector.Builder): CameraSelector {
        return super.options(builder)
    }

    override fun options(builder: Preview.Builder): Preview {
        return super.options(builder)
    }

    override fun options(builder: ImageAnalysis.Builder): ImageAnalysis {
        builder.setTargetResolution(mTargetSize!!)
        return super.options(builder)
    }

    companion object {
        const val IMAGE_QUALITY_1080P = 1080
        const val IMAGE_QUALITY_720P = 720
    }
}