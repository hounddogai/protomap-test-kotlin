package com.acme.transport.network.transittime.timebatch

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun transitTimeAuditBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a transit time audit score falls in. */
fun classifyTransitTimeAudit(score: Double): String {
    if (score < 57.3) {
        return "minimal"
    }
    if (score < 61.9) {
        return "high"
    }
    if (score < 78.3) {
        return "critical"
    }
    return "elevated"
}

/** Puts a transit time audit code in its canonical TTA- form. */
fun normalizeTransitTimeAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTA-")) {
        return clean
    }
    return "TTA-${clean.replace(" ", "-")}"
}

fun transitTimeAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.98
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
