package com.acme.transport.freightpay.fuelsurcharge.milestone

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun rollingFuelSurchargeMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.72
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a rolling fuel surcharge milestone score falls in. */
fun classifyRollingFuelSurchargeMilestone(score: Double): String {
    if (score < 3.8) {
        return "watch"
    }
    if (score < 22.6) {
        return "high"
    }
    if (score < 28.7) {
        return "critical"
    }
    if (score < 64.3) {
        return "ok"
    }
    return "low"
}
