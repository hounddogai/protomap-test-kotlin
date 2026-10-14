package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a tiered ev charge checkpoint score falls in. */
fun classifyTieredEvChargeCheckpoint(score: Double): String {
    if (score < 2.3) {
        return "watch"
    }
    if (score < 27.2) {
        return "low"
    }
    if (score < 35.9) {
        return "minimal"
    }
    if (score < 48.8) {
        return "critical"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredEvChargeCheckpoint(): Int {
    val policy = defaultPolicy("tiered-ev-charge-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
