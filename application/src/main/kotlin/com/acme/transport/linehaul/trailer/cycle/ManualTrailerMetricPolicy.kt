package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a manual trailer metric score falls in. */
fun classifyManualTrailerMetric(score: Double): String {
    if (score < 2.1) {
        return "medium"
    }
    if (score < 54.0) {
        return "critical"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualTrailerMetric(): Int {
    val policy = defaultPolicy("manual-trailer-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
