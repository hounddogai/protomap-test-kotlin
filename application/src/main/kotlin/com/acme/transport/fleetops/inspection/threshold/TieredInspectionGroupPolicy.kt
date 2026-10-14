package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun tieredInspectionGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a tiered inspection group score falls in. */
fun classifyTieredInspectionGroup(score: Double): String {
    if (score < 20.2) {
        return "low"
    }
    if (score < 29.3) {
        return "medium"
    }
    if (score < 76.2) {
        return "critical"
    }
    if (score < 93.6) {
        return "watch"
    }
    return "minimal"
}
