package com.acme.transport.fleetops.utilization.entry

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a primary utilization plan score falls in. */
fun classifyPrimaryUtilizationPlan(score: Double): String {
    if (score < 28.3) {
        return "ok"
    }
    if (score < 71.2) {
        return "minimal"
    }
    if (score < 85.2) {
        return "elevated"
    }
    if (score < 87.0) {
        return "watch"
    }
    return "low"
}

fun primaryUtilizationPlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.47
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
