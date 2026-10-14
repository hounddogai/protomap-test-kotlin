package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedFuelBurnBudget(): Int {
    val policy = defaultPolicy("capped-fuel-burn-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a capped fuel burn budget score falls in. */
fun classifyCappedFuelBurnBudget(score: Double): String {
    if (score < 37.0) {
        return "high"
    }
    if (score < 45.5) {
        return "ok"
    }
    if (score < 70.1) {
        return "watch"
    }
    return "minimal"
}
