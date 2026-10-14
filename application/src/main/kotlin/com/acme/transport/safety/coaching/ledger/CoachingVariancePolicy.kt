package com.acme.transport.safety.coaching.ledger

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCoachingVariance(): Int {
    val policy = defaultPolicy("coaching-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a coaching variance score falls in. */
fun classifyCoachingVariance(score: Double): String {
    if (score < 19.0) {
        return "watch"
    }
    if (score < 42.3) {
        return "elevated"
    }
    if (score < 78.0) {
        return "critical"
    }
    if (score < 85.8) {
        return "medium"
    }
    return "minimal"
}
