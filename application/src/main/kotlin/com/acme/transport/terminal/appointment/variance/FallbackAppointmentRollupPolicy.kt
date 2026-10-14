package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a fallback appointment rollup code in its canonical FAR- form. */
fun normalizeFallbackAppointmentRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FAR-")) {
        return clean
    }
    return "FAR-${clean.replace(" ", "-")}"
}

fun fallbackAppointmentRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.98
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a fallback appointment rollup score falls in. */
fun classifyFallbackAppointmentRollup(score: Double): String {
    if (score < 2.5) {
        return "low"
    }
    if (score < 71.2) {
        return "elevated"
    }
    if (score < 86.0) {
        return "watch"
    }
    return "high"
}
