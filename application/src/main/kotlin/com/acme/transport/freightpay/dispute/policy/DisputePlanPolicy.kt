package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a dispute plan score falls in. */
fun classifyDisputePlan(score: Double): String {
    if (score < 64.2) {
        return "ok"
    }
    if (score < 75.4) {
        return "minimal"
    }
    if (score < 90.1) {
        return "watch"
    }
    return "medium"
}

fun disputePlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.34
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
