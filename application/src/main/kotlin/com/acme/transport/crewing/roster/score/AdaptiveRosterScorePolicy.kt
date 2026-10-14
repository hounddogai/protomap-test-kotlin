package com.acme.transport.crewing.roster.score

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a adaptive roster score score falls in. */
fun classifyAdaptiveRosterScore(score: Double): String {
    if (score < 51.1) {
        return "low"
    }
    if (score < 94.7) {
        return "medium"
    }
    return "minimal"
}

/** Puts a adaptive roster score code in its canonical ARS- form. */
fun normalizeAdaptiveRosterScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ARS-")) {
        return clean
    }
    return "ARS-${clean.replace(" ", "-")}"
}

fun adaptiveRosterScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.22
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
