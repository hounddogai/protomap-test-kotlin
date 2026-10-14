package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a rolling utilization budget score falls in. */
fun classifyRollingUtilizationBudget(score: Double): String {
    if (score < 45.6) {
        return "minimal"
    }
    if (score < 69.4) {
        return "medium"
    }
    if (score < 91.1) {
        return "high"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingUtilizationBudget(): Int {
    val policy = defaultPolicy("rolling-utilization-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
