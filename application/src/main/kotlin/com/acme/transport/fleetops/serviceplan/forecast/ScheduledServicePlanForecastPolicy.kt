package com.acme.transport.fleetops.serviceplan.forecast

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeScheduledServicePlanForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun scheduledServicePlanForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.35
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a scheduled service plan forecast score falls in. */
fun classifyScheduledServicePlanForecast(score: Double): String {
    if (score < 14.2) {
        return "minimal"
    }
    if (score < 51.3) {
        return "low"
    }
    if (score < 99.1) {
        return "elevated"
    }
    return "critical"
}
