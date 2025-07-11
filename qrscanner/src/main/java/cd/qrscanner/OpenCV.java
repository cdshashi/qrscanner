package cd.qrscanner;

import android.content.Context;
import android.util.Log;

import org.opencv.android.OpenCVLoader;

public final class OpenCV {

    private static final String TAG = "OpenCV";

    private OpenCV() {
        throw new AssertionError();
    }

    /**
     * 初始化 OpenCV
     *
     * @param context
     */

    public static void initAsync(Context context) {
        if (OpenCVLoader.initLocal()) {
            Log.i(TAG, "OpenCV loaded successfully");
        } else {
            Log.e(TAG, "OpenCV initialization failed!");
        }
    }


}
