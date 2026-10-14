package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a transit time variance score falls in. */
fun classifyTransitTimeVariance(score: Double): String {
    if (score < 7.7) {
        return "elevated"
    }
    if (score < 80.4) {
        return "minimal"
    }
    if (score < 82.9) {
        return "watch"
    }
    if (score < 90.1) {
        return "high"
    }
    return "low"
}

fun transitTimeVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.7
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
