package cd.qrscanner.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.IntRange
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

class PermissionUtils  {

    companion object {
        fun checkPermission(context: Context, permission: String): Boolean {
            return ActivityCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun requestPermission(
            activity: Activity,
            permission: String,
            @IntRange(from = 0) requestCode: Int
        ) {
            requestPermissions(activity, arrayOf(permission), requestCode)
        }

        fun requestPermission(
            fragment: Fragment,
            permission: String,
            @IntRange(from = 0) requestCode: Int
        ) {
            requestPermissions(fragment, arrayOf(permission), requestCode)
        }

        fun requestPermissions(
            activity: Activity,
            permissions: Array<String>,
            @IntRange(from = 0) requestCode: Int
        ) {
            LogUtils.d("requestPermissions: $permissions")
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }

        fun requestPermissions(
            fragment: Fragment,
            permissions: Array<String>,
            @IntRange(from = 0) requestCode: Int
        ) {
            LogUtils.d("requestPermissions: $permissions")
            fragment.requestPermissions(permissions, requestCode)
        }

        fun requestPermissionsResult(
            requestPermission: String,
            permissions: Array<String>,
            grantResults: IntArray
        ): Boolean {
            val length = permissions.size
            for (i in 0 until length) {
                if (requestPermission == permissions[i]) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        return true
                    }
                }
            }
            return false
        }

        fun requestPermissionsResult(
            requestPermissions: Array<String>,
            permissions: Array<String>,
            grantResults: IntArray
        ): Boolean {
            val length = permissions.size
            for (i in 0 until length) {
                for (j in requestPermissions.indices) {
                    if (requestPermissions[j] == permissions[i]) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            return false
                        }
                    }
                }
            }
            return true
        }
    }
}