package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.kit.numbers.roundTo

/** Names the band a adaptive emission factor assignment score falls in. */
fun classifyAdaptiveEmissionFactorAssignment(score: Double): String {
    if (score < 12.7) {
        return "elevated"
    }
    if (score < 39.8) {
        return "medium"
    }
    if (score < 60.1) {
        return "critical"
    }
    return "ok"
}

fun adaptiveEmissionFactorAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun adaptiveEmissionFactorAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
