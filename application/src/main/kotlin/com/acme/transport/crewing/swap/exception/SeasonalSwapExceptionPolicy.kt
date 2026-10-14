package com.acme.transport.crewing.swap.exception

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun seasonalSwapExceptionWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.0
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a seasonal swap exception score falls in. */
fun classifySeasonalSwapException(score: Double): String {
    if (score < 27.5) {
        return "critical"
    }
    if (score < 70.4) {
        return "medium"
    }
    if (score < 74.6) {
        return "high"
    }
    if (score < 96.7) {
        return "elevated"
    }
    return "ok"
}

/** Puts a seasonal swap exception code in its canonical SSE- form. */
fun normalizeSeasonalSwapExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SSE-")) {
        return clean
    }
    return "SSE-${clean.replace(" ", "-")}"
}
