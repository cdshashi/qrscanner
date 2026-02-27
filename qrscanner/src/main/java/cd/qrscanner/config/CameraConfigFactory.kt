package cd.qrscanner.config

import android.content.Context
import androidx.camera.core.CameraSelector
import cd.qrscanner.analyzer.AspectRatioCameraConfig

class CameraConfigFactory{
     companion object {
        fun createDefaultCameraConfig(context: Context, lensFacing: Int = CameraSelector.LENS_FACING_BACK): CameraConfig {
            val resolvedLensFacing = if (lensFacing >= 0) lensFacing else CameraSelector.LENS_FACING_BACK
            val displayMetrics = context.resources.displayMetrics
            val size = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels)
            return if (size > ResolutionCameraConfig.IMAGE_QUALITY_720P) {
                var imageQuality = ResolutionCameraConfig.IMAGE_QUALITY_720P
                if (size > ResolutionCameraConfig.IMAGE_QUALITY_1080P) {
                    imageQuality = ResolutionCameraConfig.IMAGE_QUALITY_1080P
                }
                object : ResolutionCameraConfig(context, imageQuality) {
                    override fun options(builder: CameraSelector.Builder): CameraSelector {
                        builder.requireLensFacing(resolvedLensFacing)
                        return super.options(builder)
                    }
                }
            } else {
                object : AspectRatioCameraConfig(context) {
                    override fun options(builder: CameraSelector.Builder): CameraSelector {
                        builder.requireLensFacing(resolvedLensFacing)
                        return super.options(builder)
                    }
                }
            }
        }
    }
}
