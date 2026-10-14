package com.acme.transport.freightpay.accessorial.accessorialplan

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun legacyAccessorialTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.96
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun legacyAccessorialTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a legacy accessorial tier score falls in. */
fun classifyLegacyAccessorialTier(score: Double): String {
    if (score < 9.3) {
        return "watch"
    }
    if (score < 25.2) {
        return "low"
    }
    return "critical"
}
