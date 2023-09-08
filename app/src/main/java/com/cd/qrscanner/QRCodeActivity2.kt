package com.cd.qrscanner

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import cd.qrscanner.analyzer.AnalyzeResult
import cd.qrscanner.analyzer.Analyzer
import cd.qrscanner.scanner.*
import cd.qrscanner.utils.BitmapUtils.Companion.getBitMapWithRect
import cd.qrscanner.utils.PermissionUtils



open class QRCodeActivity2 : AppCompatActivity(), CameraScan.OnScanResultCallback<List<String>> {

    private var mCameraScan: CameraScan<List<String>>? = null
    private var ivFlashlight : ImageView? = null
    private var previewView : PreviewView? = null
    private var ivImage : ImageView? = null
    private var text : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        QRCodeDetector.init(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode2)
        previewView = findViewById(R.id.previewView)
        ivFlashlight = findViewById(R.id.ivFlashlight)
        ivImage = findViewById(R.id.ivImage)
        text = findViewById(R.id.text)

        mCameraScan = createCameraScan(previewView)
        initCameraScan(mCameraScan)
        startCamera()

        ivFlashlight!!.setOnClickListener { v: View? -> toggleTorchState() }

    }

    private fun toggleTorchState() {
        if (mCameraScan != null) {
            val isTorch: Boolean = mCameraScan?.isTorchEnabled == true
            mCameraScan?.enableTorch(!isTorch)
            if (ivFlashlight != null) {
                ivFlashlight!!.isSelected = !isTorch
            }
        }
    }


    fun startCamera() {
        if (mCameraScan != null) {
            if (PermissionUtils.checkPermission(this, Manifest.permission.CAMERA)) {
                mCameraScan!!.startCamera()
            } else {
                PermissionUtils.requestPermission(
                    this,
                    Manifest.permission.CAMERA,
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun releaseCamera() {
        if (mCameraScan != null) {
            mCameraScan!!.release()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            requestCameraPermissionResult(permissions, grantResults)
        }
    }


    private fun requestCameraPermissionResult(permissions: Array<String>, grantResults: IntArray) {
        if (PermissionUtils.requestPermissionsResult(
                Manifest.permission.CAMERA,
                permissions,
                grantResults
            )
        ) {
            startCamera()
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        releaseCamera()
        super.onDestroy()
    }

    private fun createCameraScan(previewView: PreviewView?): CameraScan<List<String>> {
        return BaseCameraScan(this,this, previewView!!)
    }

    private fun initCameraScan(cameraScan: CameraScan<List<String>>?) {
        cameraScan?.setAnalyzer(createAnalyzer())
            ?.bindFlashlightView(ivFlashlight)
            ?.setOnScanResultCallback(this)?.setPlayBeep(true)
    }

    override fun onScanResultCallback(result: AnalyzeResult<List<String>>) {
        mCameraScan?.setAnalyzeImage(false)
        Log.d(TAG, result.result.toString())
        val frameMetadata = result.frameMetadata
        var width = frameMetadata.width
        var height = frameMetadata.height
        if(frameMetadata.rotation == 90 || frameMetadata.rotation == 270) {
            width = frameMetadata.height
            height = frameMetadata.width
        }

        if (result is QRCodeAnalyzeResult) {
            val bitmap = getBitMapWithRect(result.bitmap,result.result,result.points)
            text?.setText(result.result.toString())
            ivImage?.setImageBitmap(bitmap)
        }
    }

    private fun createAnalyzer(): Analyzer<List<String>> {
        return ScanningAnalyzer(true)
    }


    companion object {
        const val TAG = "QRCodeActivity"
        const val CAMERA_PERMISSION_REQUEST_CODE = 0x86;
    }

}