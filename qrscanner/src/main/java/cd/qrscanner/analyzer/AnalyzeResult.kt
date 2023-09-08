package cd.qrscanner.analyzer

import android.graphics.Bitmap
import android.graphics.ImageFormat
import cd.qrscanner.utils.BitmapUtils
import cd.qrscanner.config.FrameMetadata

open class AnalyzeResult<T>(
    val imageData: ByteArray,
    val imageFormat: Int,
    val frameMetadata: FrameMetadata,
    val result: T
) {
    var bitmap: Bitmap? = null
        get() {
            require(imageFormat == ImageFormat.NV21) { "only support ImageFormat.NV21 for now." }
            if (field == null) {
                field = BitmapUtils.getBitmap(imageData, frameMetadata)
            }
            return field
        }
        private set

}