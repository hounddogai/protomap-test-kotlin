package com.acme.transport.emissions.idling.forecast

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun idlingScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.59
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a idling score score falls in. */
fun classifyIdlingScore(score: Double): String {
    if (score < 5.5) {
        return "elevated"
    }
    if (score < 22.4) {
        return "ok"
    }
    if (score < 31.6) {
        return "minimal"
    }
    if (score < 41.9) {
        return "watch"
    }
    return "medium"
}

/** Puts a idling score code in its canonical IS- form. */
fun normalizeIdlingScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IS-")) {
        return clean
    }
    return "IS-${clean.replace(" ", "-")}"
}
