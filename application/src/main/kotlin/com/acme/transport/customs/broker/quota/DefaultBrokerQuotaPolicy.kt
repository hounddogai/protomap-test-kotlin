package com.acme.transport.customs.broker.quota

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a default broker quota score falls in. */
fun classifyDefaultBrokerQuota(score: Double): String {
    if (score < 2.4) {
        return "elevated"
    }
    if (score < 7.8) {
        return "high"
    }
    if (score < 14.2) {
        return "ok"
    }
    if (score < 89.5) {
        return "medium"
    }
    return "minimal"
}

fun defaultBrokerQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.71
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
