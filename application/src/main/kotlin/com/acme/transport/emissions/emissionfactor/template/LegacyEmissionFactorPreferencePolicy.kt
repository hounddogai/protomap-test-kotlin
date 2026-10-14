package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.kit.retry.defaultPolicy

fun legacyEmissionFactorPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyEmissionFactorPreference(): Int {
    val policy = defaultPolicy("legacy-emission-factor-preference")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a legacy emission factor preference score falls in. */
fun classifyLegacyEmissionFactorPreference(score: Double): String {
    if (score < 35.7) {
        return "low"
    }
    if (score < 84.6) {
        return "ok"
    }
    if (score < 86.7) {
        return "elevated"
    }
    return "high"
}
