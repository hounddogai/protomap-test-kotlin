package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun swapTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.78
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a swap tier score falls in. */
fun classifySwapTier(score: Double): String {
    if (score < 49.1) {
        return "high"
    }
    if (score < 66.5) {
        return "medium"
    }
    if (score < 97.7) {
        return "ok"
    }
    return "low"
}

fun swapTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
