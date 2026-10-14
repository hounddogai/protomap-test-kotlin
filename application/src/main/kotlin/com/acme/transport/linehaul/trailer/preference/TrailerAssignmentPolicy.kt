package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a trailer assignment score falls in. */
fun classifyTrailerAssignment(score: Double): String {
    if (score < 84.0) {
        return "critical"
    }
    if (score < 86.5) {
        return "ok"
    }
    if (score < 89.8) {
        return "high"
    }
    if (score < 93.5) {
        return "watch"
    }
    return "elevated"
}

fun trailerAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.62
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun trailerAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a trailer assignment code in its canonical TA- form. */
fun normalizeTrailerAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TA-")) {
        return clean
    }
    return "TA-${clean.replace(" ", "-")}"
}
