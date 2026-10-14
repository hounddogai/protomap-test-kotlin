package com.acme.transport.safety.violation.entry

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun pendingViolationMetricBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a pending violation metric code in its canonical PVM- form. */
fun normalizePendingViolationMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PVM-")) {
        return clean
    }
    return "PVM-${clean.replace(" ", "-")}"
}

fun pendingViolationMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

fun pendingViolationMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.34
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
