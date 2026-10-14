package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun emissionFactorSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.74
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a emission factor summary score falls in. */
fun classifyEmissionFactorSummary(score: Double): String {
    if (score < 25.7) {
        return "medium"
    }
    if (score < 35.6) {
        return "critical"
    }
    if (score < 53.2) {
        return "low"
    }
    return "minimal"
}

/** Puts a emission factor summary code in its canonical EFS- form. */
fun normalizeEmissionFactorSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("EFS-")) {
        return clean
    }
    return "EFS-${clean.replace(" ", "-")}"
}

fun emissionFactorSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
