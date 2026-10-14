package com.acme.transport.crewing.swap.schedule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a swap quota score falls in. */
fun classifySwapQuota(score: Double): String {
    if (score < 16.1) {
        return "ok"
    }
    if (score < 36.8) {
        return "high"
    }
    if (score < 49.0) {
        return "low"
    }
    return "minimal"
}

fun swapQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.95
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
