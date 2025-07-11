package cd.qrscanner.scanner

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageProxy
import cd.qrscanner.analyzer.AnalyzeResult
import cd.qrscanner.analyzer.Analyzer
import cd.qrscanner.analyzer.Analyzer.OnAnalyzeListener
import cd.qrscanner.config.FrameMetadata
import cd.qrscanner.utils.LogUtils
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class ScanningAnalyzer @JvmOverloads constructor(private val isOutputVertices: Boolean = false) :
    Analyzer<List<String>> {
    private val queue: Queue<ByteArray> = ConcurrentLinkedQueue()
    private val joinQueue = AtomicBoolean(false)
    override fun analyze(
        imageProxy: ImageProxy,
        listener: OnAnalyzeListener<AnalyzeResult<List<String>>?>
    ) {
        if (!joinQueue.get()) {
            val imageSize = imageProxy.width * imageProxy.height
            val bytes = ByteArray(imageSize + 2 * (imageSize / 4))
            queue.add(bytes)
            joinQueue.set(true)
        }
        if (queue.isEmpty()) {
            return
        }
        val nv21Data = queue.poll()

        var result: AnalyzeResult<List<String?>>? = null
        try {
            yuv_420_888toNv21(imageProxy, nv21Data)
            val frameMetadata = FrameMetadata(
                imageProxy.width,
                imageProxy.height,
                imageProxy.imageInfo.rotationDegrees
            )
            result = detectAndDecode(nv21Data, frameMetadata, isOutputVertices)

        } catch (e: Exception) {
            LogUtils.w(e)
        }
        if (result != null) {
            joinQueue.set(false)
            listener.onSuccess(result as AnalyzeResult<List<String>>)
        } else {
            queue.add(nv21Data)
            listener.onFailure(null)
        }
    }

//    private val mAreaRectRatio: Float =  0.8f
//    private fun barCode( data : ByteArray,
//                         frameMetadata: FrameMetadata,
//                         width : Int, height : Int) : AnalyzeResult<List<String?>>?{
//        var rawResult : Result? = null
//        val mReader = MultiFormatReader()
////        mReader.setHints(TWO_DIMENSIONAL_HINTS)
//        val size: Int = (Math.min(width, height) * mAreaRectRatio).toInt()
//        val left: Int = (width - size) / 2
//        val top: Int = (height - size) / 2
//
//        val source = PlanarYUVLuminanceSource(data, width, height, left, top, size, size, false)
//        rawResult = decodeInternal(source, mReader)
//
//        if (rawResult == null){
//            val rotatedData = ByteArray(data.size)
//            for (y in 0 until height) {
//                for (x in 0 until width) {
//                    rotatedData[x * height + height - y - 1] = data[x + y * width]
//                }
//            }
//            rawResult = decodeInternal(
//                PlanarYUVLuminanceSource(
//                    rotatedData,
//                    height,
//                    width,
//                    top,
//                    left,
//                    size,
//                    size,
//                    false
//                ),mReader
//            )
//            if (rawResult == null ) {
//                rawResult = decodeInternal(
//                    source.invert(),
//                    mReader
//                )
//            }
//        }
//        if (rawResult != null){
//            val item : MutableList<String> = mutableListOf()
//            item.add(rawResult.text)
//          return QRCodeAnalyzeResult(data, ImageFormat.NV21, frameMetadata, item)
//        }
//        return null
//    }
//
//    private fun decodeInternal(source: LuminanceSource,mReader : MultiFormatReader): Result? {
//        var result: Result? = null
//        try {
//            try {
//                result = mReader.decodeWithState(BinaryBitmap(HybridBinarizer(source)))
//            } catch (e: java.lang.Exception) {
//            }
//            if (result == null) {
//                result = mReader.decodeWithState(BinaryBitmap(GlobalHistogramBinarizer(source)))
//            }
//        } catch (e: java.lang.Exception) {
//        }
//        return result
//    }

    private fun detectAndDecode(
        nv21: ByteArray,
        frameMetadata: FrameMetadata,
        isOutputVertices: Boolean
    ): AnalyzeResult<List<String?>>? {
        val mat = Mat(
            frameMetadata.height + frameMetadata.height / 2,
            frameMetadata.width,
            CvType.CV_8UC1
        )
        mat.put(0, 0, nv21)
        val bgrMat = Mat()
        Imgproc.cvtColor(mat, bgrMat, Imgproc.COLOR_YUV2BGR_NV21)
        mat.release()
        rotation(bgrMat, frameMetadata.rotation)
        if (isOutputVertices) {
            val points: List<Mat> = ArrayList()
            val result = QRCodeDetector.detectAndDecode(bgrMat, points)
            if (result != null && result.isNotEmpty()) {
                return QRCodeAnalyzeResult(nv21, ImageFormat.NV21, frameMetadata, result, points)
            }
        } else {
            val result = QRCodeDetector.detectAndDecode(bgrMat)

            if (result != null && result.isNotEmpty()) {
                return QRCodeAnalyzeResult(nv21, ImageFormat.NV21, frameMetadata, result)
            }
        }
        return null
    }

    private fun rotation(mat: Mat, rotation: Int) {
        if (rotation == ROTATION_90) {
            Core.transpose(mat, mat)
            Core.flip(mat, mat, 1)
        } else if (rotation == ROTATION_180) {
            Core.flip(mat, mat, 0)
            Core.flip(mat, mat, 1)
        } else if (rotation == ROTATION_270) {
            Core.transpose(mat, mat)
            Core.flip(mat, mat, 0)
        }
    }

    private fun yuv_420_888toNv21(image: ImageProxy, nv21: ByteArray) {
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]
        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer
        yBuffer.rewind()
        uBuffer.rewind()
        vBuffer.rewind()
        val ySize = yBuffer.remaining()
        var position = 0

        // Add the full y buffer to the array. If rowStride > 1, some padding may be skipped.
        for (row in 0 until image.height) {
            yBuffer[nv21, position, image.width]
            position += image.width
            yBuffer.position(Math.min(ySize, yBuffer.position() - image.width + yPlane.rowStride))
        }
        val chromaHeight = image.height / 2
        val chromaWidth = image.width / 2
        val vRowStride = vPlane.rowStride
        val uRowStride = uPlane.rowStride
        val vPixelStride = vPlane.pixelStride
        val uPixelStride = uPlane.pixelStride

        // Interleave the u and v frames, filling up the rest of the buffer. Use two line buffers to
        // perform faster bulk gets from the byte buffers.
        val vLineBuffer = ByteArray(vRowStride)
        val uLineBuffer = ByteArray(uRowStride)
        for (row in 0 until chromaHeight) {
            vBuffer[vLineBuffer, 0, Math.min(vRowStride, vBuffer.remaining())]
            uBuffer[uLineBuffer, 0, Math.min(uRowStride, uBuffer.remaining())]
            var vLineBufferPosition = 0
            var uLineBufferPosition = 0
            for (col in 0 until chromaWidth) {
                nv21[position++] = vLineBuffer[vLineBufferPosition]
                nv21[position++] = uLineBuffer[uLineBufferPosition]
                vLineBufferPosition += vPixelStride
                uLineBufferPosition += uPixelStride
            }
        }
    }

    companion object {
        private const val ROTATION_90 = 90
        private const val ROTATION_180 = 180
        private const val ROTATION_270 = 270
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyzeBarCode(
        imageProxy: ImageProxy,
        listener: OnAnalyzeListener<AnalyzeResult<List<String>>?>
    ) {
//        if (!joinQueue.get()) {
//            val imageSize = imageProxy.width * imageProxy.height
//            val bytes = ByteArray(imageSize + 2 * (imageSize / 4))
//            queue.add(bytes)
//            joinQueue.set(true)
//        }
//        if (queue.isEmpty()) {
//            return
//        }
//        val nv21Data = queue.poll()
//
//        var result: AnalyzeResult<List<String?>>? = null
//        try {
//            yuv_420_888toNv21(imageProxy, nv21Data)
            val frameMetadata = FrameMetadata(
                imageProxy.width,
                imageProxy.height,
                imageProxy.imageInfo.rotationDegrees
            )
//
//            val rotation = imageProxy.imageInfo.rotationDegrees
//            val width = imageProxy.width
//            val height = imageProxy.height
//
//            if (rotation == 90 || rotation == 270) {
//                val rotatedData = ByteArray(nv21Data.size)
//                for (y in 0 until height) {
//                    for (x in 0 until width) {
//                        rotatedData[x * height + height - y - 1] = nv21Data[x + y * width]
//                    }
//                }
//                result = barCode(rotatedData,frameMetadata, height, width)
//            } else {
//                result = barCode(nv21Data,frameMetadata, width, height)
//            }
//
//        } catch (e: Exception) {
//            LogUtils.w(e)
//        }
//        if (result != null) {
//            joinQueue.set(false)
//            listener.onSuccess(result as AnalyzeResult<List<String>>)
//        } else {
//            queue.add(nv21Data)
//            listener.onFailure(null)
//        }

        val img = imageProxy.image
        if (img != null) {
            val inputImage = InputImage.fromMediaImage(img, imageProxy.imageInfo.rotationDegrees)

            // Process image searching for barcodes
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()

            val scanner = BarcodeScanning.getClient()

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    val item : MutableList<String?> = mutableListOf()
                    if (barcodes.size > 0) {
                        barcodes?.forEach {
                            item.add(it.rawValue)
                        }
                        val imageSize = imageProxy.width * imageProxy.height
                        val bytes = ByteArray(imageSize + 2 * (imageSize / 4))
                        val result =
                            QRCodeAnalyzeResult(bytes, ImageFormat.NV21, frameMetadata, item)
                        listener.onSuccess(result as AnalyzeResult<List<String>>)
                    }

                }
        }

//        imageProxy.close()
    }
}