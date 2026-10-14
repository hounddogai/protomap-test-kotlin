package com.acme.transport.emissions.fuelburn.adjustment

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a adaptive fuel burn adjustment score falls in. */
fun classifyAdaptiveFuelBurnAdjustment(score: Double): String {
    if (score < 7.4) {
        return "low"
    }
    if (score < 35.9) {
        return "watch"
    }
    if (score < 40.4) {
        return "minimal"
    }
    if (score < 77.5) {
        return "high"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveFuelBurnAdjustment(): Int {
    val policy = defaultPolicy("adaptive-fuel-burn-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
