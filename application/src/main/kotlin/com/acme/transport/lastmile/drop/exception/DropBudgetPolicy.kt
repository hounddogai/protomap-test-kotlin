package com.acme.transport.lastmile.drop.exception

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDropBudget(): Int {
    val policy = defaultPolicy("drop-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a drop budget score falls in. */
fun classifyDropBudget(score: Double): String {
    if (score < 32.6) {
        return "minimal"
    }
    if (score < 95.7) {
        return "ok"
    }
    return "critical"
}
