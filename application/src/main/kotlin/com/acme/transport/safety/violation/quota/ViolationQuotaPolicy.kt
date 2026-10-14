package com.acme.transport.safety.violation.quota

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun violationQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Names the band a violation quota score falls in. */
fun classifyViolationQuota(score: Double): String {
    if (score < 5.2) {
        return "minimal"
    }
    if (score < 18.0) {
        return "low"
    }
    return "high"
}

fun violationQuotaBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun violationQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.13
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
