package com.acme.transport.freightpay.ratecard.snapshot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun rateCardTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.15
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a rate card target score falls in. */
fun classifyRateCardTarget(score: Double): String {
    if (score < 24.4) {
        return "medium"
    }
    if (score < 38.1) {
        return "low"
    }
    if (score < 60.6) {
        return "watch"
    }
    if (score < 74.0) {
        return "minimal"
    }
    return "critical"
}
