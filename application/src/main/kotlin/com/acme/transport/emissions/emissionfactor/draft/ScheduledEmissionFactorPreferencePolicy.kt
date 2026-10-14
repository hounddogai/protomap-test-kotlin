package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a scheduled emission factor preference score falls in. */
fun classifyScheduledEmissionFactorPreference(score: Double): String {
    if (score < 14.6) {
        return "watch"
    }
    if (score < 38.9) {
        return "low"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledEmissionFactorPreference(): Int {
    val policy = defaultPolicy("scheduled-emission-factor-preference")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
