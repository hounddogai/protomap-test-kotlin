package com.acme.transport.network.transittime.notice

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a transit time tier score falls in. */
fun classifyTransitTimeTier(score: Double): String {
    if (score < 20.6) {
        return "low"
    }
    if (score < 59.5) {
        return "elevated"
    }
    if (score < 77.5) {
        return "watch"
    }
    if (score < 77.5) {
        return "medium"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTransitTimeTier(): Int {
    val policy = defaultPolicy("transit-time-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
