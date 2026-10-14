package com.acme.transport.terminal.dwell.dwellreview

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a legacy dwell review score falls in. */
fun classifyLegacyDwellReview(score: Double): String {
    if (score < 34.6) {
        return "medium"
    }
    if (score < 37.1) {
        return "critical"
    }
    if (score < 70.4) {
        return "watch"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyDwellReview(): Int {
    val policy = defaultPolicy("legacy-dwell-review")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
