package com.acme.transport.customs.clearance.milestone

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a lenient clearance metric score falls in. */
fun classifyLenientClearanceMetric(score: Double): String {
    if (score < 15.3) {
        return "critical"
    }
    if (score < 22.1) {
        return "elevated"
    }
    if (score < 77.9) {
        return "watch"
    }
    if (score < 97.2) {
        return "medium"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientClearanceMetric(): Int {
    val policy = defaultPolicy("lenient-clearance-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
