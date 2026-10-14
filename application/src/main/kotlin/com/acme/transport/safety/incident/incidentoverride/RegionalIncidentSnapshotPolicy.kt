package com.acme.transport.safety.incident.incidentoverride

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun regionalIncidentSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.1
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a regional incident snapshot score falls in. */
fun classifyRegionalIncidentSnapshot(score: Double): String {
    if (score < 13.4) {
        return "watch"
    }
    if (score < 31.8) {
        return "ok"
    }
    if (score < 41.2) {
        return "medium"
    }
    return "critical"
}
