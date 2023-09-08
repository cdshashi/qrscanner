package cd.qrscanner.utils

import android.util.Log
import java.util.*

class LogUtils {


    companion object {
        const val TAG = "CameraScan"
        const val VERTICAL = "|"
        var isShowLog = true
        var priority = 1

        /**
         * Priority constant for the println method;use System.out.println
         */
        const val PRINTLN = 1

        /**
         * Priority constant for the println method; use Log.v.
         */
        const val VERBOSE = 2

        /**
         * Priority constant for the println method; use Log.d.
         */
        const val DEBUG = 3

        /**
         * Priority constant for the println method; use Log.i.
         */
        const val INFO = 4

        /**
         * Priority constant for the println method; use Log.w.
         */
        const val WARN = 5

        /**
         * Priority constant for the println method; use Log.e.
         */
        const val ERROR = 6

        /**
         * Priority constant for the println method.use Log.wtf.
         */
        const val ASSERT = 7
        const val TAG_FORMAT = "%s.%s(%s:%d)"

        /**
         * 根据堆栈生成TAG
         *
         * @return TAG|className.methodName(fileName:lineNumber)
         */
        private fun generateTag(caller: StackTraceElement): String {
            var tag = TAG_FORMAT
            var callerClazzName = caller.className
            callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1)
            tag = String.format(
                Locale.getDefault(),
                tag,
                callerClazzName,
                caller.methodName,
                caller.fileName,
                caller.lineNumber
            )
            return StringBuilder().append(TAG).append(VERTICAL).append(tag).toString()
        }

        /**
         * 获取堆栈
         *
         * @param n n=0		VMStack
         * n=1		Thread
         * n=3		CurrentStack
         * n=4		CallerStack
         * ...
         * @return
         */
        fun getStackTraceElement(n: Int): StackTraceElement {
            return Thread.currentThread().stackTrace[n]
        }

        private val callerStackLogTag: String
            private get() = generateTag(getStackTraceElement(5))

        /**
         * @param t
         * @return
         */
        private fun getStackTraceString(t: Throwable): String {
            return Log.getStackTraceString(t)
        }
        // -----------------------------------Log.v
        /**
         * Log.v
         *
         * @param msg
         */
        fun v(msg: String) {
            if (isShowLog && priority <= VERBOSE) Log.v(
                callerStackLogTag, msg
            )
        }

        fun v(t: Throwable) {
            if (isShowLog && priority <= VERBOSE) Log.v(
                callerStackLogTag, getStackTraceString(t)
            )
        }

        fun v(msg: String, t: Throwable?) {
            if (isShowLog && priority <= VERBOSE) Log.v(
                callerStackLogTag, msg, t
            )
        }
        // -----------------------------------Log.d
        /**
         * Log.d
         *
         * @param msg
         */
        fun d(msg: String) {
            if (isShowLog && priority <= DEBUG) Log.d(
                callerStackLogTag, msg
            )
        }

        fun d(t: Throwable) {
            if (isShowLog && priority <= DEBUG) Log.d(
                callerStackLogTag, getStackTraceString(t)
            )
        }

        fun d(msg: String, t: Throwable?) {
            if (isShowLog && priority <= DEBUG) Log.d(
                callerStackLogTag, msg, t
            )
        }
        // -----------------------------------Log.i
        /**
         * Log.i
         *
         * @param msg
         */
        fun i(msg: String) {
            if (isShowLog && priority <= INFO) Log.i(
                callerStackLogTag, msg
            )
        }

        fun i(t: Throwable) {
            if (isShowLog && priority <= INFO) Log.i(
                callerStackLogTag, getStackTraceString(t)
            )
        }

        fun i(msg: String, t: Throwable?) {
            if (isShowLog && priority <= INFO) Log.i(
                callerStackLogTag, msg, t
            )
        }
        // -----------------------------------Log.w
        /**
         * Log.w
         *
         * @param msg
         */
        fun w(msg: String) {
            if (isShowLog && priority <= WARN) Log.w(
                callerStackLogTag, msg
            )
        }

        fun w(t: Throwable) {
            if (isShowLog && priority <= WARN) Log.w(
                callerStackLogTag, getStackTraceString(t)
            )
        }

        fun w(msg: String, t: Throwable?) {
            if (isShowLog && priority <= WARN) Log.w(
                callerStackLogTag, msg, t
            )
        }
        // -----------------------------------Log.e
        /**
         * Log.e
         *
         * @param msg
         */
        fun e(msg: String) {
            if (isShowLog && priority <= ERROR) Log.e(
                callerStackLogTag, msg
            )
        }

        fun e(t: Throwable) {
            if (isShowLog && priority <= ERROR) Log.e(
                callerStackLogTag, getStackTraceString(t)
            )
        }

        fun e(msg: String, t: Throwable?) {
            if (isShowLog && priority <= ERROR) Log.e(
                callerStackLogTag, msg, t
            )
        }
        // -----------------------------------Log.wtf
        /**
         * Log.wtf
         *
         * @param msg
         */
        fun wtf(msg: String) {
            if (isShowLog && priority <= ASSERT) Log.wtf(
                callerStackLogTag, msg
            )
        }

        fun wtf(t: Throwable) {
            if (isShowLog && priority <= ASSERT) Log.wtf(
                callerStackLogTag, getStackTraceString(t)
            )
        }

        fun wtf(msg: String, t: Throwable?) {
            if (isShowLog && priority <= ASSERT) Log.wtf(
                callerStackLogTag, msg, t
            )
        }
        // -----------------------------------System.out.print
        /**
         * System.out.print
         *
         * @param msg
         */
        fun print(msg: String?) {
            if (isShowLog && priority <= PRINTLN) print(msg)
        }

        fun print(obj: Any?) {
            if (isShowLog && priority <= PRINTLN) print(obj)
        }
        // -----------------------------------System.out.printf
        /**
         * System.out.printf
         *
         * @param msg
         */
        fun printf(msg: String?) {
            if (isShowLog && priority <= PRINTLN) System.out.printf(msg)
        }
        // -----------------------------------System.out.println
        /**
         * System.out.println
         *
         * @param msg
         */
        fun println(msg: String?) {
            if (isShowLog && priority <= PRINTLN) println(msg)
        }

        fun println(obj: Any?) {
            if (isShowLog && priority <= PRINTLN) println(obj)
        }
    }
}