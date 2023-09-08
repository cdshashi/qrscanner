package cd.qrscanner.analyzer

import android.content.Context
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import cd.qrscanner.config.CameraConfig
import cd.qrscanner.scanner.CameraScan
import cd.qrscanner.utils.LogUtils
import java.util.*

open class AspectRatioCameraConfig(context: Context) : CameraConfig() {
    private var mAspectRatio = 0

    init {
        initTargetAspectRatio(context)
    }

    private fun initTargetAspectRatio(context: Context) {
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        LogUtils.d(String.format(Locale.getDefault(), "displayMetrics: %dx%d", width, height))
        val ratio = Math.max(width, height) / Math.min(width, height).toFloat()
        mAspectRatio =
            if (Math.abs(ratio - CameraScan.ASPECT_RATIO_4_3) < Math.abs(ratio - CameraScan.ASPECT_RATIO_16_9)) {
                AspectRatio.RATIO_4_3
            } else {
                AspectRatio.RATIO_16_9
            }
        LogUtils.d("aspectRatio: $mAspectRatio")
    }

    override fun options(builder: CameraSelector.Builder): CameraSelector {
        return super.options(builder)
    }

    override fun options(builder: Preview.Builder): Preview {
        return super.options(builder)
    }

    override fun options(builder: ImageAnalysis.Builder): ImageAnalysis {
        builder.setTargetAspectRatio(mAspectRatio)
        return super.options(builder)
    }
}