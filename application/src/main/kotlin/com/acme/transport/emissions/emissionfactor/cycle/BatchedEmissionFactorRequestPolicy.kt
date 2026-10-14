package com.acme.transport.emissions.emissionfactor.cycle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a batched emission factor request score falls in. */
fun classifyBatchedEmissionFactorRequest(score: Double): String {
    if (score < 48.5) {
        return "critical"
    }
    if (score < 51.6) {
        return "elevated"
    }
    if (score < 80.3) {
        return "watch"
    }
    if (score < 81.5) {
        return "high"
    }
    return "medium"
}

/** Puts a batched emission factor request code in its canonical BEFR- form. */
fun normalizeBatchedEmissionFactorRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BEFR-")) {
        return clean
    }
    return "BEFR-${clean.replace(" ", "-")}"
}

fun batchedEmissionFactorRequestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.03
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
