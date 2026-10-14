package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a manual rate card cycle score falls in. */
fun classifyManualRateCardCycle(score: Double): String {
    if (score < 11.7) {
        return "ok"
    }
    if (score < 20.9) {
        return "medium"
    }
    if (score < 25.5) {
        return "elevated"
    }
    if (score < 77.7) {
        return "critical"
    }
    return "high"
}

fun manualRateCardCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.31
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
