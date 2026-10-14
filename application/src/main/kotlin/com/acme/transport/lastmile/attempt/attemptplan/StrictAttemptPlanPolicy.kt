package com.acme.transport.lastmile.attempt.attemptplan

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a strict attempt plan score falls in. */
fun classifyStrictAttemptPlan(score: Double): String {
    if (score < 29.3) {
        return "high"
    }
    if (score < 53.8) {
        return "ok"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictAttemptPlan(): Int {
    val policy = defaultPolicy("strict-attempt-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
