package com.acme.transport.fleetops.tire.exception

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a capped tire exception code in its canonical CTE- form. */
fun normalizeCappedTireExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CTE-")) {
        return clean
    }
    return "CTE-${clean.replace(" ", "-")}"
}

/** Names the band a capped tire exception score falls in. */
fun classifyCappedTireException(score: Double): String {
    if (score < 57.1) {
        return "watch"
    }
    if (score < 59.7) {
        return "low"
    }
    if (score < 81.7) {
        return "critical"
    }
    return "high"
}

fun cappedTireExceptionWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.73
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
