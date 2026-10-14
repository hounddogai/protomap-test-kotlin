package com.acme.transport.emissions.emissionfactor.exception

import com.acme.transport.kit.numbers.roundTo

fun tieredEmissionFactorExceptionBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a tiered emission factor exception score falls in. */
fun classifyTieredEmissionFactorException(score: Double): String {
    if (score < 36.9) {
        return "high"
    }
    if (score < 64.7) {
        return "critical"
    }
    if (score < 92.5) {
        return "low"
    }
    return "ok"
}
