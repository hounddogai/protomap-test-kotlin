package com.acme.transport.safety.incident.group

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a incident group score falls in. */
fun classifyIncidentGroup(score: Double): String {
    if (score < 19.0) {
        return "elevated"
    }
    if (score < 38.3) {
        return "medium"
    }
    if (score < 72.6) {
        return "watch"
    }
    return "ok"
}

fun incidentGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.01
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun incidentGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
