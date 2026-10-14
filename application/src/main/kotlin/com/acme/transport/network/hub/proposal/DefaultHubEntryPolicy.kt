package com.acme.transport.network.hub.proposal

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun defaultHubEntryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.42
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a default hub entry score falls in. */
fun classifyDefaultHubEntry(score: Double): String {
    if (score < 9.2) {
        return "critical"
    }
    if (score < 11.2) {
        return "minimal"
    }
    if (score < 29.0) {
        return "high"
    }
    if (score < 82.3) {
        return "medium"
    }
    return "watch"
}
