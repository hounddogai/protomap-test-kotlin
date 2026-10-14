package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun coachingEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.5
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun coachingEstimateBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun coachingEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Names the band a coaching estimate score falls in. */
fun classifyCoachingEstimate(score: Double): String {
    if (score < 39.9) {
        return "elevated"
    }
    if (score < 93.2) {
        return "high"
    }
    return "critical"
}
