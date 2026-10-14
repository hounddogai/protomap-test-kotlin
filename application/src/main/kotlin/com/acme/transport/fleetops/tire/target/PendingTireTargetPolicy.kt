package com.acme.transport.fleetops.tire.target

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun pendingTireTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.66
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a pending tire target score falls in. */
fun classifyPendingTireTarget(score: Double): String {
    if (score < 3.8) {
        return "critical"
    }
    if (score < 27.7) {
        return "minimal"
    }
    if (score < 82.9) {
        return "high"
    }
    if (score < 87.1) {
        return "ok"
    }
    return "medium"
}
