package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.kit.numbers.roundTo

/** Names the band a emission factor budget score falls in. */
fun classifyEmissionFactorBudget(score: Double): String {
    if (score < 10.7) {
        return "critical"
    }
    if (score < 20.5) {
        return "minimal"
    }
    if (score < 28.7) {
        return "watch"
    }
    if (score < 47.1) {
        return "medium"
    }
    return "elevated"
}

fun emissionFactorBudgetBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
