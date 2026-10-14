package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.kit.numbers.roundTo

/** Names the band a seasonal emission factor cycle score falls in. */
fun classifySeasonalEmissionFactorCycle(score: Double): String {
    if (score < 3.4) {
        return "high"
    }
    if (score < 89.9) {
        return "elevated"
    }
    return "low"
}

fun seasonalEmissionFactorCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Puts a seasonal emission factor cycle code in its canonical SEFC- form. */
fun normalizeSeasonalEmissionFactorCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SEFC-")) {
        return clean
    }
    return "SEFC-${clean.replace(" ", "-")}"
}

fun seasonalEmissionFactorCycleBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
