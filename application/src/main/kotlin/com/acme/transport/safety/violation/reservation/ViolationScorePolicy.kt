package com.acme.transport.safety.violation.reservation

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun violationScoreBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a violation score code in its canonical VS- form. */
fun normalizeViolationScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("VS-")) {
        return clean
    }
    return "VS-${clean.replace(" ", "-")}"
}

fun violationScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun violationScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.93
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
