package com.acme.transport.fleetops.utilization.utilizationsegment

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a weighted utilization segment code in its canonical WUS- form. */
fun normalizeWeightedUtilizationSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WUS-")) {
        return clean
    }
    return "WUS-${clean.replace(" ", "-")}"
}

fun weightedUtilizationSegmentBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedUtilizationSegment(): Int {
    val policy = defaultPolicy("weighted-utilization-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
