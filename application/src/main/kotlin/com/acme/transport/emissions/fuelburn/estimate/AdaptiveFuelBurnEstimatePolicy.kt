package com.acme.transport.emissions.fuelburn.estimate

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveFuelBurnEstimate(): Int {
    val policy = defaultPolicy("adaptive-fuel-burn-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a adaptive fuel burn estimate score falls in. */
fun classifyAdaptiveFuelBurnEstimate(score: Double): String {
    if (score < 50.2) {
        return "high"
    }
    if (score < 88.3) {
        return "watch"
    }
    return "medium"
}
