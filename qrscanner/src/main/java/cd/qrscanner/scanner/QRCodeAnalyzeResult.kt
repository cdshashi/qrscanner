package cd.qrscanner.scanner

import cd.qrscanner.config.FrameMetadata
import cd.qrscanner.analyzer.AnalyzeResult
import org.opencv.core.Mat

class QRCodeAnalyzeResult<T> : AnalyzeResult<T> {
    var points: List<Mat>? = null
        private set

    constructor(
        imageData: ByteArray,
        imageFormat: Int,
        frameMetadata: FrameMetadata,
        result: T
    ) : super(imageData, imageFormat, frameMetadata, result) {
    }

    constructor(
        imageData: ByteArray,
        imageFormat: Int,
        frameMetadata: FrameMetadata,
        result: T,
        points: List<Mat>?
    ) : super(imageData, imageFormat, frameMetadata, result) {
        this.points = points
    }
}