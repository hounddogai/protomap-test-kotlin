package com.acme.transport.network.hub.batch

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun hubEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.77
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a hub estimate score falls in. */
fun classifyHubEstimate(score: Double): String {
    if (score < 10.8) {
        return "ok"
    }
    if (score < 53.8) {
        return "elevated"
    }
    return "medium"
}
