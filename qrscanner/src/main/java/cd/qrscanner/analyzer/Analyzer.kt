package cd.qrscanner.analyzer

import androidx.camera.core.ImageProxy

interface Analyzer<T> {
    fun analyze(imageProxy: ImageProxy, listener: OnAnalyzeListener<AnalyzeResult<T>?>)
    fun analyzeBarCode(imageProxy: ImageProxy, listener: OnAnalyzeListener<AnalyzeResult<T>?>)
    interface OnAnalyzeListener<T> {
        fun onSuccess(result: T)
        fun onFailure(e: Exception?)
    }
}