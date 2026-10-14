package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun scheduledNearMissSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.39
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a scheduled near miss segment score falls in. */
fun classifyScheduledNearMissSegment(score: Double): String {
    if (score < 16.9) {
        return "watch"
    }
    if (score < 62.1) {
        return "elevated"
    }
    if (score < 80.8) {
        return "minimal"
    }
    if (score < 87.3) {
        return "low"
    }
    return "medium"
}
