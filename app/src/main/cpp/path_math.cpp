#include <jni.h>
#include <vector>
#include <algorithm>

// Centered moving-average smoothing of a 2D path.
// Kept in C++ to practice the Android NDK / JNI boundary.
extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_lleyton_flightdeck_nativebridge_PathMath_smoothPath(
        JNIEnv* env, jobject /* this */, jfloatArray xsArr, jfloatArray ysArr, jint window) {
    const jsize n = env->GetArrayLength(xsArr);
    std::vector<float> xs(n), ys(n), out(2 * n);
    env->GetFloatArrayRegion(xsArr, 0, n, xs.data());
    env->GetFloatArrayRegion(ysArr, 0, n, ys.data());

    const int half = std::max(0, static_cast<int>(window) / 2);
    for (int i = 0; i < n; ++i) {
        const int lo = std::max(0, i - half);
        const int hi = std::min(static_cast<int>(n) - 1, i + half);
        float sx = 0.f, sy = 0.f;
        for (int j = lo; j <= hi; ++j) { sx += xs[j]; sy += ys[j]; }
        const float count = static_cast<float>(hi - lo + 1);
        out[2 * i] = sx / count;
        out[2 * i + 1] = sy / count;
    }

    jfloatArray result = env->NewFloatArray(2 * n);
    env->SetFloatArrayRegion(result, 0, 2 * n, out.data());
    return result;
}
