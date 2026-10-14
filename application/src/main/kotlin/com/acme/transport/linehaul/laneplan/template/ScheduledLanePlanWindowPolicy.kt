package com.acme.transport.linehaul.laneplan.template

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun scheduledLanePlanWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.82
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a scheduled lane plan window score falls in. */
fun classifyScheduledLanePlanWindow(score: Double): String {
    if (score < 4.4) {
        return "critical"
    }
    if (score < 5.4) {
        return "minimal"
    }
    if (score < 68.8) {
        return "elevated"
    }
    if (score < 89.1) {
        return "low"
    }
    return "high"
}
