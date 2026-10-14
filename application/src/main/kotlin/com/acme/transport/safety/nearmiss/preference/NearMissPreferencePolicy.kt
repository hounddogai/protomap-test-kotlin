package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a near miss preference code in its canonical NMP- form. */
fun normalizeNearMissPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NMP-")) {
        return clean
    }
    return "NMP-${clean.replace(" ", "-")}"
}

fun nearMissPreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.97
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a near miss preference score falls in. */
fun classifyNearMissPreference(score: Double): String {
    if (score < 20.3) {
        return "high"
    }
    if (score < 28.1) {
        return "critical"
    }
    if (score < 50.8) {
        return "minimal"
    }
    if (score < 63.7) {
        return "ok"
    }
    return "low"
}
