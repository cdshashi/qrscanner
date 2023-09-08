package com.cd.qrscanner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import cd.qrscanner.scanner.CameraScan

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.text)
        val text1 = findViewById<Button>(R.id.text1)
        button.setOnClickListener {
            startActivityForResult(QRCodeActivity::class.java)
        }
        text1.setOnClickListener {
            startActivityForResult(QRCodeActivity2::class.java)
        }
    }
    private fun startActivityForResult(clazz: Class<*>) {
        val options = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.alpha_in, R.anim.alpha_out)
        startActivityForResult(Intent(this, clazz), REQUEST_CODE_QRCODE, options.toBundle())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_QRCODE -> processQRCodeResult(data)
            }
        }
    }

    private fun processQRCodeResult(intent: Intent?) {
        CameraScan.parseScanResult(intent)?.let {
            Log.d(CameraScan.SCAN_RESULT, it)
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {

        const val REQUEST_CODE_QRCODE = 0x10
        const val REQUEST_CODE_REQUEST_EXTERNAL_STORAGE = 0x11
        const val REQUEST_CODE_PICK_PHOTO = 0x12
    }
}