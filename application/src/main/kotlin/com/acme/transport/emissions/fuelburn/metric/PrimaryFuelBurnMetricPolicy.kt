package com.acme.transport.emissions.fuelburn.metric

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a primary fuel burn metric score falls in. */
fun classifyPrimaryFuelBurnMetric(score: Double): String {
    if (score < 1.6) {
        return "minimal"
    }
    if (score < 26.0) {
        return "watch"
    }
    if (score < 77.5) {
        return "high"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryFuelBurnMetric(): Int {
    val policy = defaultPolicy("primary-fuel-burn-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
