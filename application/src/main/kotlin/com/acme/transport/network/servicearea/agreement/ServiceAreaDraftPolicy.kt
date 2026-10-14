package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a service area draft score falls in. */
fun classifyServiceAreaDraft(score: Double): String {
    if (score < 10.0) {
        return "medium"
    }
    if (score < 34.3) {
        return "elevated"
    }
    if (score < 61.4) {
        return "minimal"
    }
    return "low"
}

/** Puts a service area draft code in its canonical SAD- form. */
fun normalizeServiceAreaDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAD-")) {
        return clean
    }
    return "SAD-${clean.replace(" ", "-")}"
}

fun serviceAreaDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.8
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
