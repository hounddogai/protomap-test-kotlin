package com.acme.transport.terminal.crossdock.proposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a cross dock bundle score falls in. */
fun classifyCrossDockBundle(score: Double): String {
    if (score < 5.7) {
        return "low"
    }
    if (score < 27.9) {
        return "medium"
    }
    if (score < 76.6) {
        return "watch"
    }
    if (score < 88.4) {
        return "critical"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCrossDockBundle(): Int {
    val policy = defaultPolicy("cross-dock-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeCrossDockBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun crossDockBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}
