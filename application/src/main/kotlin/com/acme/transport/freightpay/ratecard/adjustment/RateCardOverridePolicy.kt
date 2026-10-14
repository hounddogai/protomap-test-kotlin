package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun rateCardOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a rate card override score falls in. */
fun classifyRateCardOverride(score: Double): String {
    if (score < 2.4) {
        return "low"
    }
    if (score < 30.5) {
        return "medium"
    }
    if (score < 55.4) {
        return "high"
    }
    if (score < 57.9) {
        return "elevated"
    }
    return "minimal"
}
