package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a fallback emission factor reservation score falls in. */
fun classifyFallbackEmissionFactorReservation(score: Double): String {
    if (score < 39.4) {
        return "minimal"
    }
    if (score < 73.3) {
        return "medium"
    }
    if (score < 92.0) {
        return "elevated"
    }
    if (score < 92.7) {
        return "critical"
    }
    return "watch"
}

fun fallbackEmissionFactorReservationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.74
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a fallback emission factor reservation code in its canonical FEFR- form. */
fun normalizeFallbackEmissionFactorReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FEFR-")) {
        return clean
    }
    return "FEFR-${clean.replace(" ", "-")}"
}
