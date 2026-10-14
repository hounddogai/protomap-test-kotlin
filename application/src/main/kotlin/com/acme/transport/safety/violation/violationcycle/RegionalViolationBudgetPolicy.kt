package com.acme.transport.safety.violation.violationcycle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun regionalViolationBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

fun regionalViolationBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.86
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a regional violation budget score falls in. */
fun classifyRegionalViolationBudget(score: Double): String {
    if (score < 26.5) {
        return "critical"
    }
    if (score < 40.8) {
        return "minimal"
    }
    if (score < 57.4) {
        return "high"
    }
    if (score < 75.1) {
        return "elevated"
    }
    return "watch"
}
