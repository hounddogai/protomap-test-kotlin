package com.acme.transport.emissions.emissionfactor.claim

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a emission factor tier code in its canonical EFT- form. */
fun normalizeEmissionFactorTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("EFT-")) {
        return clean
    }
    return "EFT-${clean.replace(" ", "-")}"
}

fun emissionFactorTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.12
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a emission factor tier score falls in. */
fun classifyEmissionFactorTier(score: Double): String {
    if (score < 70.9) {
        return "high"
    }
    if (score < 73.6) {
        return "low"
    }
    if (score < 90.8) {
        return "medium"
    }
    if (score < 97.9) {
        return "elevated"
    }
    return "ok"
}
