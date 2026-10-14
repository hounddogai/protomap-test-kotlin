package com.acme.transport.network.transittime.entry

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun transitTimeCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.56
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a transit time checkpoint score falls in. */
fun classifyTransitTimeCheckpoint(score: Double): String {
    if (score < 46.6) {
        return "elevated"
    }
    if (score < 68.0) {
        return "low"
    }
    if (score < 79.8) {
        return "medium"
    }
    if (score < 97.1) {
        return "high"
    }
    return "minimal"
}
