package com.acme.transport.lastmile.attempt.milestone

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAttemptTarget(): Int {
    val policy = defaultPolicy("attempt-target")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a attempt target score falls in. */
fun classifyAttemptTarget(score: Double): String {
    if (score < 12.1) {
        return "watch"
    }
    if (score < 62.1) {
        return "high"
    }
    return "critical"
}
