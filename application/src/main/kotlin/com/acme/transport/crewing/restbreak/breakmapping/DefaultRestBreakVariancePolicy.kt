package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a default rest break variance code in its canonical DRBV- form. */
fun normalizeDefaultRestBreakVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DRBV-")) {
        return clean
    }
    return "DRBV-${clean.replace(" ", "-")}"
}

fun defaultRestBreakVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.9
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a default rest break variance score falls in. */
fun classifyDefaultRestBreakVariance(score: Double): String {
    if (score < 25.4) {
        return "elevated"
    }
    if (score < 32.2) {
        return "high"
    }
    if (score < 94.6) {
        return "medium"
    }
    return "low"
}
