package com.acme.transport.crewing.availability.claim

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun availabilityMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

fun availabilityMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.11
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a availability metric score falls in. */
fun classifyAvailabilityMetric(score: Double): String {
    if (score < 9.7) {
        return "minimal"
    }
    if (score < 49.2) {
        return "low"
    }
    if (score < 70.4) {
        return "critical"
    }
    return "watch"
}

fun availabilityMetricBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
