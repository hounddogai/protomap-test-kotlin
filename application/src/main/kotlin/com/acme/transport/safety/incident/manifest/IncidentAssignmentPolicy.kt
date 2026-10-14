package com.acme.transport.safety.incident.manifest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a incident assignment score falls in. */
fun classifyIncidentAssignment(score: Double): String {
    if (score < 5.5) {
        return "high"
    }
    if (score < 28.6) {
        return "critical"
    }
    if (score < 93.8) {
        return "watch"
    }
    if (score < 97.3) {
        return "elevated"
    }
    return "low"
}

fun incidentAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.5
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
