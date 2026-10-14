package com.acme.transport.customs.clearance.threshold

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a seasonal clearance profile score falls in. */
fun classifySeasonalClearanceProfile(score: Double): String {
    if (score < 14.0) {
        return "low"
    }
    if (score < 33.1) {
        return "minimal"
    }
    if (score < 90.4) {
        return "ok"
    }
    if (score < 98.4) {
        return "watch"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalClearanceProfile(): Int {
    val policy = defaultPolicy("seasonal-clearance-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
