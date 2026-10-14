package com.acme.transport.crewing.availability.segment

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAvailabilitySegment(): Int {
    val policy = defaultPolicy("availability-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a availability segment score falls in. */
fun classifyAvailabilitySegment(score: Double): String {
    if (score < 51.1) {
        return "critical"
    }
    if (score < 60.5) {
        return "ok"
    }
    if (score < 72.8) {
        return "high"
    }
    return "minimal"
}
