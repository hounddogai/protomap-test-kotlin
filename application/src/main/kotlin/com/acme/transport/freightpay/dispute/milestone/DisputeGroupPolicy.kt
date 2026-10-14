package com.acme.transport.freightpay.dispute.milestone

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a dispute group score falls in. */
fun classifyDisputeGroup(score: Double): String {
    if (score < 10.5) {
        return "critical"
    }
    if (score < 48.3) {
        return "high"
    }
    if (score < 67.5) {
        return "ok"
    }
    if (score < 81.2) {
        return "watch"
    }
    return "minimal"
}

fun disputeGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.89
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun disputeGroupBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
