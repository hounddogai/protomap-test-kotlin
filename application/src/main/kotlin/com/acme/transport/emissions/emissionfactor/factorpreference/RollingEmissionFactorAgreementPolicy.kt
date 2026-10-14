package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.kit.numbers.roundTo

/** Names the band a rolling emission factor agreement score falls in. */
fun classifyRollingEmissionFactorAgreement(score: Double): String {
    if (score < 11.4) {
        return "critical"
    }
    if (score < 60.3) {
        return "high"
    }
    if (score < 88.4) {
        return "low"
    }
    if (score < 91.1) {
        return "medium"
    }
    return "ok"
}

fun rollingEmissionFactorAgreementBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
