package com.acme.transport.emissions.fuelburn.window

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a tiered fuel burn rollup score falls in. */
fun classifyTieredFuelBurnRollup(score: Double): String {
    if (score < 44.6) {
        return "low"
    }
    if (score < 78.8) {
        return "critical"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredFuelBurnRollup(): Int {
    val policy = defaultPolicy("tiered-fuel-burn-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
