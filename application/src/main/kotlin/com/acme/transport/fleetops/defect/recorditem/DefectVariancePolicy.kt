package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun defectVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefectVariance(): Int {
    val policy = defaultPolicy("defect-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a defect variance score falls in. */
fun classifyDefectVariance(score: Double): String {
    if (score < 33.7) {
        return "ok"
    }
    if (score < 45.3) {
        return "elevated"
    }
    return "low"
}

fun defectVarianceBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
