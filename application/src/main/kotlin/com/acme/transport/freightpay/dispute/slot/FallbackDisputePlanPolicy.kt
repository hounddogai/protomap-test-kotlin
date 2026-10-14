package com.acme.transport.freightpay.dispute.slot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun fallbackDisputePlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a fallback dispute plan score falls in. */
fun classifyFallbackDisputePlan(score: Double): String {
    if (score < 32.1) {
        return "medium"
    }
    if (score < 88.6) {
        return "ok"
    }
    if (score < 91.2) {
        return "watch"
    }
    if (score < 98.4) {
        return "high"
    }
    return "elevated"
}
