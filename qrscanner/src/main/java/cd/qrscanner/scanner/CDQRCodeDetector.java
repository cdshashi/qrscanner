package cd.qrscanner.scanner;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.objdetect.QRCodeDetector;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.List;

public class CDQRCodeDetector {

    private final QRCodeDetector detector;

    public CDQRCodeDetector() {
        this.detector = new QRCodeDetector();
    }


    /**
     * Both detects and decodes QR code.
     * To simplify the usage, there is a only API: detectAndDecode
     *
     * @param img supports grayscale or color (BGR) image.
     * @param points optional output array of vertices of the found QR code quadrangle. Will be
     * empty if not found.
     * @return list of decoded string.
     */

    public List<String> detectAndDecode(Mat img, List<Mat> points) {
        Mat pointsMat = new Mat();
        List<String> decodedInfo = new ArrayList<>();

        // Detect and decode multiple QR codes
        boolean success = detector.detectAndDecodeMulti(img, decodedInfo, pointsMat);

        // Convert pointsMat (MatOfPoint2f) to List<Mat>
        if (success && !pointsMat.empty() && pointsMat.type() == CvType.CV_32SC2 && pointsMat.cols() == 1) {
            Converters.Mat_to_vector_Mat(pointsMat, points);
        }

        // Release resources
        pointsMat.release();
        img.release();  // Optional: only if you're done with the image

        return decodedInfo;
    }

    /**
     * Both detects and decodes QR code.
     * To simplify the usage, there is a only API: detectAndDecode
     *
     * @param img supports grayscale or color (BGR) image.
     * empty if not found.
     * @return list of decoded string.
     */

    public List<String> detectAndDecode(Mat img) {
        Mat pointsMat = new Mat();
        // Detect and decode a single QR code
        String result = detector.detectAndDecode(img, pointsMat);
        // Release resources
        pointsMat.release();
        img.release(); // Optional, if you're done with the image

        // Wrap result in a list
        List<String> results = new ArrayList<>();
        if (result != null && !result.isEmpty()) {
            results.add(result);
        }
        return results;
    }

}
