package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun pendingIncidentRequestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.61
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a pending incident request score falls in. */
fun classifyPendingIncidentRequest(score: Double): String {
    if (score < 6.2) {
        return "high"
    }
    if (score < 19.0) {
        return "ok"
    }
    if (score < 98.9) {
        return "minimal"
    }
    return "watch"
}
