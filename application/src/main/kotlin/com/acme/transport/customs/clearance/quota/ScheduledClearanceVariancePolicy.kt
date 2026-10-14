package com.acme.transport.customs.clearance.quota

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a scheduled clearance variance score falls in. */
fun classifyScheduledClearanceVariance(score: Double): String {
    if (score < 23.9) {
        return "critical"
    }
    if (score < 32.8) {
        return "elevated"
    }
    return "minimal"
}

fun scheduledClearanceVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.33
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
