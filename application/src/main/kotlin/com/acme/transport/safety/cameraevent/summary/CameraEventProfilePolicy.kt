package com.acme.transport.safety.cameraevent.summary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun cameraEventProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

fun mergeCameraEventProfileLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a camera event profile score falls in. */
fun classifyCameraEventProfile(score: Double): String {
    if (score < 32.5) {
        return "high"
    }
    if (score < 79.0) {
        return "ok"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCameraEventProfile(): Int {
    val policy = defaultPolicy("camera-event-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
