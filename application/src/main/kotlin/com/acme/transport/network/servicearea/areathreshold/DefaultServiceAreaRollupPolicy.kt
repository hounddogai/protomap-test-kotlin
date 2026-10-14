package com.acme.transport.network.servicearea.areathreshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun defaultServiceAreaRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun mergeDefaultServiceAreaRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a default service area rollup score falls in. */
fun classifyDefaultServiceAreaRollup(score: Double): String {
    if (score < 28.2) {
        return "low"
    }
    if (score < 96.0) {
        return "ok"
    }
    if (score < 99.7) {
        return "watch"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultServiceAreaRollup(): Int {
    val policy = defaultPolicy("default-service-area-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
