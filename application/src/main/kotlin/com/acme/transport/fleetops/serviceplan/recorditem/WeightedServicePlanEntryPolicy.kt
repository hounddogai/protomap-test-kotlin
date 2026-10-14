package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a weighted service plan entry score falls in. */
fun classifyWeightedServicePlanEntry(score: Double): String {
    if (score < 9.0) {
        return "medium"
    }
    if (score < 20.2) {
        return "critical"
    }
    if (score < 53.6) {
        return "high"
    }
    return "watch"
}

fun weightedServicePlanEntryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.35
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
