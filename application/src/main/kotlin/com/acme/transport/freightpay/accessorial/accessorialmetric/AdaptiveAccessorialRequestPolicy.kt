package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun adaptiveAccessorialRequestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.1
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a adaptive accessorial request score falls in. */
fun classifyAdaptiveAccessorialRequest(score: Double): String {
    if (score < 10.0) {
        return "watch"
    }
    if (score < 12.0) {
        return "medium"
    }
    if (score < 40.0) {
        return "low"
    }
    if (score < 96.6) {
        return "high"
    }
    return "minimal"
}
