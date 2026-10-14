package com.acme.transport.terminal.sortation.estimate

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun fallbackSortationVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.28
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a fallback sortation variance score falls in. */
fun classifyFallbackSortationVariance(score: Double): String {
    if (score < 65.9) {
        return "minimal"
    }
    if (score < 70.7) {
        return "watch"
    }
    return "critical"
}
