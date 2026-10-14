package com.acme.transport.network.zone.review

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalZoneReview(): Int {
    val policy = defaultPolicy("global-zone-review")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a global zone review score falls in. */
fun classifyGlobalZoneReview(score: Double): String {
    if (score < 21.5) {
        return "watch"
    }
    if (score < 40.4) {
        return "medium"
    }
    if (score < 91.1) {
        return "minimal"
    }
    return "high"
}
