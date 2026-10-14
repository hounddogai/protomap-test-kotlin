package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.numbers.roundTo

/** Names the band a seasonal violation quota score falls in. */
fun classifySeasonalViolationQuota(score: Double): String {
    if (score < 49.7) {
        return "medium"
    }
    if (score < 55.0) {
        return "low"
    }
    if (score < 76.8) {
        return "high"
    }
    return "elevated"
}

fun seasonalViolationQuotaBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun seasonalViolationQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}
