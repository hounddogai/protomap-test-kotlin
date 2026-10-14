package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun serviceAreaAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.29
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a service area adjustment score falls in. */
fun classifyServiceAreaAdjustment(score: Double): String {
    if (score < 58.6) {
        return "watch"
    }
    if (score < 84.5) {
        return "elevated"
    }
    return "critical"
}
