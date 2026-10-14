package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a emission factor manifest code in its canonical EFM- form. */
fun normalizeEmissionFactorManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("EFM-")) {
        return clean
    }
    return "EFM-${clean.replace(" ", "-")}"
}

fun emissionFactorManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.83
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a emission factor manifest score falls in. */
fun classifyEmissionFactorManifest(score: Double): String {
    if (score < 11.5) {
        return "ok"
    }
    if (score < 57.0) {
        return "medium"
    }
    if (score < 60.4) {
        return "watch"
    }
    if (score < 70.9) {
        return "minimal"
    }
    return "low"
}
