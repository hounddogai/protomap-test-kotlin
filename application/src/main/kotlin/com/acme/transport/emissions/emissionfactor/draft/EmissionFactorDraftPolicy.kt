package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeEmissionFactorDraft(): Int {
    val policy = defaultPolicy("emission-factor-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a emission factor draft score falls in. */
fun classifyEmissionFactorDraft(score: Double): String {
    if (score < 61.8) {
        return "high"
    }
    if (score < 62.1) {
        return "critical"
    }
    if (score < 94.5) {
        return "ok"
    }
    if (score < 95.3) {
        return "medium"
    }
    return "low"
}
