package com.lleyton.flightdeck.nativebridge

/**
 * Kotlin side of the C++ (NDK) path smoother. The real work happens in
 * app/src/main/cpp/path_math.cpp, called through JNI.
 */
object PathMath {
    init { System.loadLibrary("flightdeck") }

    /** Returns smoothed points as [x0, y0, x1, y1, ...] using a moving average. */
    external fun smoothPath(xs: FloatArray, ys: FloatArray, window: Int): FloatArray
}
