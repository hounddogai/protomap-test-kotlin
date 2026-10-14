package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun fallbackFuelSurchargeMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.56
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a fallback fuel surcharge milestone score falls in. */
fun classifyFallbackFuelSurchargeMilestone(score: Double): String {
    if (score < 27.2) {
        return "high"
    }
    if (score < 38.6) {
        return "low"
    }
    if (score < 56.0) {
        return "minimal"
    }
    if (score < 83.1) {
        return "medium"
    }
    return "critical"
}
