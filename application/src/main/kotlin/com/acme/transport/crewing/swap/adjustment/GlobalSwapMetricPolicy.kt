package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun globalSwapMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.78
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a global swap metric score falls in. */
fun classifyGlobalSwapMetric(score: Double): String {
    if (score < 65.2) {
        return "watch"
    }
    if (score < 77.7) {
        return "medium"
    }
    return "low"
}
