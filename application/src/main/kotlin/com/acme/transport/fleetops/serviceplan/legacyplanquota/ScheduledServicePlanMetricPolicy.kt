package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.kit.numbers.roundTo

/** Names the band a scheduled service plan metric score falls in. */
fun classifyScheduledServicePlanMetric(score: Double): String {
    if (score < 39.0) {
        return "high"
    }
    if (score < 74.5) {
        return "elevated"
    }
    if (score < 95.3) {
        return "minimal"
    }
    return "watch"
}

fun scheduledServicePlanMetricBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun scheduledServicePlanMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Puts a scheduled service plan metric code in its canonical SSPM- form. */
fun normalizeScheduledServicePlanMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SSPM-")) {
        return clean
    }
    return "SSPM-${clean.replace(" ", "-")}"
}
