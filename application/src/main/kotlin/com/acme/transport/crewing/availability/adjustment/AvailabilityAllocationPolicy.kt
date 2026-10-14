package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a availability allocation score falls in. */
fun classifyAvailabilityAllocation(score: Double): String {
    if (score < 47.6) {
        return "watch"
    }
    if (score < 60.4) {
        return "elevated"
    }
    if (score < 78.1) {
        return "low"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAvailabilityAllocation(): Int {
    val policy = defaultPolicy("availability-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
