package cd.qrscanner.scanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import cd.qrscanner.OpenCV;

public final class QRCodeDetector {

    private static final String TAG = "CDQRCodeDetector";
    private static final String MODEL_DIR = "models";
    private static final String DETECT_PROTOTXT = "detect.prototxt";
    private static final String DETECT_CAFFEMODEL = "detect.caffemodel";
    private static final String SR_PROTOTXT = "sr.prototxt";
    private static final String SR_CAFFEMODEL = "sr.caffemodel";

//    private static final CDQRCodeDetector detector = new CDQRCodeDetector();

    public static void init(Context context) {
        OpenCV.initAsync(context);
        initWeChatQRCode(context.getApplicationContext());
    }

    private static void initWeChatQRCode(Context context) {
        try {
            String saveDirPath = getExternalFilesDir(context, MODEL_DIR);
            if (saveDirPath != null) {
            String[] models = new String[]{DETECT_PROTOTXT, DETECT_CAFFEMODEL, SR_PROTOTXT, SR_CAFFEMODEL};

            File saveDir = new File(saveDirPath);
            boolean exists = saveDir.exists();

            if (exists) {
                for (int i = 0; i < models.length; i++) {
                    if (!new File(saveDirPath, models[i]).exists()) {
                        exists = false;
                        break;
                    }
                }
            } else {
                saveDir.mkdirs();
            }
            if (!exists) {
                for (String model : models) {
                    InputStream inputStream = context.getAssets()
                            .open(MODEL_DIR + File.separatorChar + model);
                    File saveFile = new File(saveDir, model);
                    FileOutputStream outputStream = new FileOutputStream(saveFile);
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }
                    outputStream.flush();
                    inputStream.close();
                    outputStream.close();
                    Log.d(TAG, "file: " + saveFile.getAbsolutePath());
                }
            }
        }
        } catch (Throwable e) {
            // e.printStackTrace();
        }
    }

 private static String getExternalFilesDir(Context context, String path) {
        File[] files = context.getExternalFilesDirs(path);
        if (files != null && files.length > 0 && files[0] != null) {
            return files[0].getAbsolutePath();
        }

        File file = context.getExternalFilesDir(path);
        if (file == null) {
            File internalDir = context.getFilesDir();
            if (internalDir != null) {
                file = new File(internalDir, path);
            }
        }

        return (file != null) ? file.getAbsolutePath() : null;
    }


    public static List<String> detectAndDecode(Bitmap bitmap) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        return detectAndDecode(mat);
    }


    public static List<String> detectAndDecode(Bitmap bitmap, List<Mat> points) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        return detectAndDecode(mat, points);
    }


    public static List<String> detectAndDecode(Mat img) {
        return new CDQRCodeDetector().detectAndDecode(img);
    }


    public static List<String> detectAndDecode(Mat img, List<Mat> points) {
        return new CDQRCodeDetector().detectAndDecode(img, points);
    }


}
