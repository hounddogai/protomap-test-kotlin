package com.acme.transport.emissions.fuelburn.variance

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFuelBurnEntry(): Int {
    val policy = defaultPolicy("fuel-burn-entry")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a fuel burn entry score falls in. */
fun classifyFuelBurnEntry(score: Double): String {
    if (score < 7.2) {
        return "low"
    }
    if (score < 20.3) {
        return "watch"
    }
    if (score < 49.9) {
        return "elevated"
    }
    if (score < 71.0) {
        return "critical"
    }
    return "medium"
}
