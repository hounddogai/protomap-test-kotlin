package com.acme.transport.safety.coaching.rollup

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a default coaching budget score falls in. */
fun classifyDefaultCoachingBudget(score: Double): String {
    if (score < 48.7) {
        return "medium"
    }
    if (score < 75.7) {
        return "critical"
    }
    if (score < 84.5) {
        return "high"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultCoachingBudget(): Int {
    val policy = defaultPolicy("default-coaching-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
