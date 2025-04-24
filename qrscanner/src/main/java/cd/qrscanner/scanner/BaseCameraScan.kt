package cd.qrscanner.scanner

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Point
import android.graphics.Rect
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View
import androidx.annotation.FloatRange
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import cd.qrscanner.analyzer.AmbientLightManager
import cd.qrscanner.analyzer.AnalyzeResult
import cd.qrscanner.analyzer.Analyzer
import cd.qrscanner.analyzer.Analyzer.OnAnalyzeListener
import cd.qrscanner.config.CameraConfig
import cd.qrscanner.config.CameraConfigFactory
import cd.qrscanner.utils.BeepManager
import cd.qrscanner.utils.LogUtils
import com.google.android.gms.tasks.Task
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors


class BaseCameraScan<T>(
    private val mContext: Context,
    private val mLifecycleOwner: LifecycleOwner,
    private val mPreviewView: PreviewView
) : CameraScan<T>() {
    private var mCameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    override var camera: Camera? = null
        private set
    private var mCameraConfig: CameraConfig? = null
    private var mAnalyzer: Analyzer<T>? = null

    @Volatile
    private var isAnalyze = true

    @Volatile
    private var isAnalyzeResult = false
    private var flashlightView: View? = null
    private var mResultLiveData: MutableLiveData<AnalyzeResult<T>?>? = null
    private var mOnScanResultCallback: OnScanResultCallback<List<String>>? = null
    private var mOnAnalyzeListener: OnAnalyzeListener<AnalyzeResult<T>?>? = null
    private var mBeepManager: BeepManager? = null
    private var mAmbientLightManager: AmbientLightManager? = null
    private var mLastHoveTapTime: Long = 0
    private var isClickTap = false
    private var mDownX = 0f
    private var mDownY = 0f


    private val mOnScaleGestureListener: OnScaleGestureListener =
        object : SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scale = detector.scaleFactor
                if (camera != null) {
                    val ratio = camera!!.cameraInfo.zoomState.value!!.zoomRatio
                    zoomTo(ratio * scale)
                    return true
                }
                return false
            }
        }

    init {
        initData()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initData() {
        mResultLiveData = MutableLiveData()
        mResultLiveData!!.observe(mLifecycleOwner) { result: AnalyzeResult<T>? ->
            if (result != null) {
                handleAnalyzeResult(result)
            } else if (mOnScanResultCallback != null) {
                mOnScanResultCallback!!.onScanResultFailure()
            }
        }
        mOnAnalyzeListener = object : OnAnalyzeListener<AnalyzeResult<T>?> {
            override fun onSuccess(result: AnalyzeResult<T>?) {
                mResultLiveData!!.postValue(result)
            }

            override fun onFailure(e: Exception?) {
                mResultLiveData!!.postValue(null)
            }
        }
        val scaleGestureDetector = ScaleGestureDetector(mContext, mOnScaleGestureListener)
        mPreviewView.setOnTouchListener { v: View?, event: MotionEvent ->
            handlePreviewViewClickTap(event)
            if (isNeedTouchZoom) {
                return@setOnTouchListener scaleGestureDetector.onTouchEvent(event)
            }
            false
        }
        mBeepManager = BeepManager(mContext)
        mAmbientLightManager = AmbientLightManager(mContext)
        if (mAmbientLightManager != null) {
            mAmbientLightManager!!.register()
            mAmbientLightManager?.setOnLightSensorEventListener(object :
                AmbientLightManager.OnLightSensorEventListener{
                override fun onSensorChanged(dark: Boolean, lightLux: Float) {
                    if (flashlightView != null) {
                    if (dark) {
                        if (flashlightView!!.visibility != View.VISIBLE) {
                            flashlightView!!.visibility = View.VISIBLE
                            flashlightView!!.isSelected = isTorchEnabled
                        }
                    } else if (flashlightView!!.visibility == View.VISIBLE && !isTorchEnabled) {
                        flashlightView!!.visibility = View.INVISIBLE
                        flashlightView!!.isSelected = false
                    }
                }
                }
            })
        }
    }

    private fun handlePreviewViewClickTap(event: MotionEvent) {
        if (event.pointerCount == 1) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isClickTap = true
                    mDownX = event.x
                    mDownY = event.y
                    mLastHoveTapTime = System.currentTimeMillis()
                }
                MotionEvent.ACTION_MOVE -> isClickTap =
                    distance(mDownX, mDownY, event.x, event.y) < HOVER_TAP_SLOP
                MotionEvent.ACTION_UP -> if (isClickTap && mLastHoveTapTime + HOVER_TAP_TIMEOUT > System.currentTimeMillis()) {
                    startFocusAndMetering(event.x, event.y)
                }
            }
        }
    }

    private fun distance(aX: Float, aY: Float, bX: Float, bY: Float): Float {
        val xDiff = aX - bX
        val yDiff = aY - bY
        return Math.sqrt((xDiff * xDiff + yDiff * yDiff).toDouble()).toFloat()
    }

    private fun startFocusAndMetering(x: Float, y: Float) {
        if (camera != null) {
            val point = mPreviewView.meteringPointFactory.createPoint(x, y)
            val focusMeteringAction = FocusMeteringAction.Builder(point).build()
            if (camera!!.cameraInfo.isFocusMeteringSupported(focusMeteringAction)) {
                camera!!.cameraControl.startFocusAndMetering(focusMeteringAction)
                LogUtils.d("startFocusAndMetering: $x,$y")
            }
        }
    }

    override fun setCameraConfig(cameraConfig: CameraConfig?): CameraScan<*> {
        if (cameraConfig != null) {
            mCameraConfig = cameraConfig
        }
        return this
    }

    override fun startCamera() {
        if (mCameraConfig == null) {
            mCameraConfig = CameraConfigFactory.createDefaultCameraConfig(mContext, -1)
        }
        LogUtils.d("CameraConfig: " + mCameraConfig!!.javaClass.simpleName)
        mCameraProviderFuture = ProcessCameraProvider.getInstance(mContext)
        mCameraProviderFuture!!.addListener({
            try {
                val cameraSelector = mCameraConfig!!.options(CameraSelector.Builder())
                val preview = mCameraConfig!!.options(Preview.Builder())
                preview.setSurfaceProvider(mPreviewView.surfaceProvider)
                val imageAnalysis = mCameraConfig!!.options(
                    ImageAnalysis.Builder()
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                )
                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { image: ImageProxy ->
                    if (isAnalyze && !isAnalyzeResult && mAnalyzer != null) {
                        mAnalyzer!!.analyzeBarCode(image, mOnAnalyzeListener!!)
                        mAnalyzer!!.analyze(image, mOnAnalyzeListener!!)
                    }
                    image.close()
                }
                if (camera != null) {
                    mCameraProviderFuture!!.get().unbindAll()
                }
                camera = mCameraProviderFuture!!.get()
                    .bindToLifecycle(mLifecycleOwner, cameraSelector, preview, imageAnalysis)
            } catch (e: Exception) {
                LogUtils.e(e)
            }
        }, ContextCompat.getMainExecutor(mContext))
    }

    @Synchronized
    private fun handleAnalyzeResult(result: AnalyzeResult<T>) {
        if (isAnalyzeResult || !isAnalyze) {
            return
        }
        isAnalyzeResult = true
        if (mBeepManager != null) {
            mBeepManager!!.playBeepSoundAndVibrate()
        }
        if (mOnScanResultCallback != null) {
            mOnScanResultCallback!!.onScanResultCallback(result as AnalyzeResult<List<String>>)
        }
        isAnalyzeResult = false
    }

    override fun stopCamera() {
        if (mCameraProviderFuture != null) {
            try {
                mCameraProviderFuture!!.get().unbindAll()
            } catch (e: Exception) {
                LogUtils.e(e)
            }
        }
    }

    override fun setAnalyzeImage(analyze: Boolean): CameraScan<*> {
        isAnalyze = analyze
        return this
    }

    override fun setAnalyzer(analyzer: Analyzer<T>?): CameraScan<*> {
        mAnalyzer = analyzer
        return this
    }

    override fun zoomIn() {
        if (camera != null) {
            val ratio = cameraInfo.zoomState.value!!.zoomRatio + ZOOM_STEP_SIZE
            val maxRatio = cameraInfo.zoomState.value!!.maxZoomRatio
            if (ratio <= maxRatio) {
                camera!!.cameraControl.setZoomRatio(ratio)
            }
        }
    }

    override fun zoomOut() {
        if (camera != null) {
            val ratio = cameraInfo.zoomState.value!!.zoomRatio - ZOOM_STEP_SIZE
            val minRatio = cameraInfo.zoomState.value!!.minZoomRatio
            if (ratio >= minRatio) {
                camera!!.cameraControl.setZoomRatio(ratio)
            }
        }
    }

    override fun zoomTo(ratio: Float) {
        if (camera != null) {
            val zoomState = cameraInfo.zoomState.value
            val maxRatio = zoomState!!.maxZoomRatio
            val minRatio = zoomState.minZoomRatio
            val zoom = Math.max(Math.min(ratio, maxRatio), minRatio)
            camera!!.cameraControl.setZoomRatio(zoom)
        }
    }

    override fun lineZoomIn() {
        if (camera != null) {
            val zoom = cameraInfo.zoomState.value!!.linearZoom + ZOOM_STEP_SIZE
            if (zoom <= 1f) {
                camera!!.cameraControl.setLinearZoom(zoom)
            }
        }
    }

    override fun lineZoomOut() {
        if (camera != null) {
            val zoom = cameraInfo.zoomState.value!!.linearZoom - ZOOM_STEP_SIZE
            if (zoom >= 0f) {
                camera!!.cameraControl.setLinearZoom(zoom)
            }
        }
    }

    override fun lineZoomTo(@FloatRange(from = 0.0, to = 1.0) linearZoom: Float) {
        if (camera != null) {
            camera!!.cameraControl.setLinearZoom(linearZoom)
        }
    }

    override fun enableTorch(torch: Boolean) {
        if (camera != null && hasFlashUnit()) {
            camera!!.cameraControl.enableTorch(torch)
        }
    }

    override val isTorchEnabled: Boolean
        get() = if (camera != null) {
            cameraInfo.torchState.value == TorchState.ON
        } else false

    override fun hasFlashUnit(): Boolean {
        return if (camera != null) {
            cameraInfo.hasFlashUnit()
        } else mContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    override fun setVibrate(vibrate: Boolean): CameraScan<*> {
        if (mBeepManager != null) {
            mBeepManager!!.setVibrate(vibrate)
        }
        return this
    }

    override fun setPlayBeep(playBeep: Boolean): CameraScan<*> {
        if (mBeepManager != null) {
            mBeepManager!!.setPlayBeep(playBeep)
        }
        return this
    }

    override fun setOnScanResultCallback(callback: OnScanResultCallback<List<String>>): CameraScan<*> {
        mOnScanResultCallback = callback
        return this
    }

    /**
     * CameraInfo
     *
     * @return [CameraInfo]
     */
    private val cameraInfo: CameraInfo
        private get() = camera!!.cameraInfo

    override fun release() {
        isAnalyze = false
        flashlightView = null
        if (mAmbientLightManager != null) {
            mAmbientLightManager!!.unregister()
        }
        if (mBeepManager != null) {
            mBeepManager!!.close()
        }
        stopCamera()
    }

    override fun bindFlashlightView(flashlightView: View?,enableSensor : Boolean): CameraScan<*> {
        this.flashlightView = flashlightView
        if (mAmbientLightManager != null ) {
            mAmbientLightManager!!.isLightSensorEnabled = (flashlightView != null && enableSensor)
            if(!enableSensor){
                 mAmbientLightManager!!.unregister()
                 flashlightView!!.visibility = View.VISIBLE
            }
        }
        return this
    }

    override fun setDarkLightLux(lightLux: Float): CameraScan<*> {
        if (mAmbientLightManager != null) {
            mAmbientLightManager!!.setDarkLightLux(lightLux)
        }
        return this
    }

    override fun setBrightLightLux(lightLux: Float): CameraScan<*> {
        if (mAmbientLightManager != null) {
            mAmbientLightManager!!.setBrightLightLux(lightLux)
        }
        return this
    }

    companion object {
        /**
         * Defines the maximum duration in milliseconds between a touch pad
         * touch and release for a given touch to be considered a tap (click) as
         * opposed to a hover movement gesture.
         */
        private const val HOVER_TAP_TIMEOUT = 150

        /**
         * Defines the maximum distance in pixels that a touch pad touch can move
         * before being released for it to be considered a tap (click) as opposed
         * to a hover movement gesture.
         */
        private const val HOVER_TAP_SLOP = 20
        private const val ZOOM_STEP_SIZE = 0.1f
    }
}
