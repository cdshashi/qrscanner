package cd.qrscanner.config

class FrameMetadata(val width: Int, val height: Int, val rotation: Int) {

    override fun toString(): String {
        return "FrameMetadata{" +
                "width=" + width +
                ", height=" + height +
                ", rotation=" + rotation +
                '}'
    }
}