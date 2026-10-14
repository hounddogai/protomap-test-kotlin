package com.acme.transport.emissions.emissionfactor.cycle

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeEmissionFactorCycle(): Int {
    val policy = defaultPolicy("emission-factor-cycle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a emission factor cycle score falls in. */
fun classifyEmissionFactorCycle(score: Double): String {
    if (score < 34.3) {
        return "medium"
    }
    if (score < 47.2) {
        return "watch"
    }
    if (score < 98.0) {
        return "low"
    }
    return "high"
}

/** Puts a emission factor cycle code in its canonical EFC- form. */
fun normalizeEmissionFactorCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("EFC-")) {
        return clean
    }
    return "EFC-${clean.replace(" ", "-")}"
}
