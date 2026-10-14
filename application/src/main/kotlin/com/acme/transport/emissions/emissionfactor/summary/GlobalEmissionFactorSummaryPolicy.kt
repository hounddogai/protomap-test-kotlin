package com.acme.transport.emissions.emissionfactor.summary

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a global emission factor summary score falls in. */
fun classifyGlobalEmissionFactorSummary(score: Double): String {
    if (score < 35.1) {
        return "watch"
    }
    if (score < 43.1) {
        return "ok"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalEmissionFactorSummary(): Int {
    val policy = defaultPolicy("global-emission-factor-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
