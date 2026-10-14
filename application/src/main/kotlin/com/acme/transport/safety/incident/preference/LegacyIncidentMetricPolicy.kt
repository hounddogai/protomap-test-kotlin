package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun legacyIncidentMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.61
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a legacy incident metric score falls in. */
fun classifyLegacyIncidentMetric(score: Double): String {
    if (score < 40.6) {
        return "high"
    }
    if (score < 46.0) {
        return "medium"
    }
    if (score < 55.8) {
        return "critical"
    }
    if (score < 78.6) {
        return "elevated"
    }
    return "watch"
}
