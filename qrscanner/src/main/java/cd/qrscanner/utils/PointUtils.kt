package cd.qrscanner.utils

import android.graphics.Point
import java.util.*

class PointUtils {
    companion object {
        @JvmOverloads
        fun transform(
            point: Point,
            srcWidth: Int,
            srcHeight: Int,
            destWidth: Int,
            destHeight: Int,
            isFit: Boolean = false
        ): Point {
            return transform(point.x, point.y, srcWidth, srcHeight, destWidth, destHeight, isFit)
        }

        @JvmOverloads
        fun transform(
            x: Int,
            y: Int,
            srcWidth: Int,
            srcHeight: Int,
            destWidth: Int,
            destHeight: Int,
            isFit: Boolean = false
        ): Point {
            LogUtils.d(
                String.format(
                    Locale.getDefault(),
                    "transform: %d,%d | %d,%d",
                    srcWidth,
                    srcHeight,
                    destWidth,
                    destHeight
                )
            )
            val widthRatio = destWidth * 1.0f / srcWidth
            val heightRatio = destHeight * 1.0f / srcHeight
            val point = Point()
            if (isFit) {
                val ratio = Math.min(widthRatio, heightRatio)
                val left = Math.abs(srcWidth * ratio - destWidth) / 2
                val top = Math.abs(srcHeight * ratio - destHeight) / 2
                point.x = (x * ratio + left).toInt()
                point.y = (y * ratio + top).toInt()
            } else {
                val ratio = Math.max(widthRatio, heightRatio)
                val left = Math.abs(srcWidth * ratio - destWidth) / 2
                val top = Math.abs(srcHeight * ratio - destHeight) / 2
                point.x = (x * ratio - left).toInt()
                point.y = (y * ratio - top).toInt()
            }
            return point
        }
    }
}