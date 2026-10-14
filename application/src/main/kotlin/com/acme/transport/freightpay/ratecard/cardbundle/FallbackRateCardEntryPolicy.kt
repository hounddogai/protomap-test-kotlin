package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun fallbackRateCardEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

fun fallbackRateCardEntryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.27
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a fallback rate card entry score falls in. */
fun classifyFallbackRateCardEntry(score: Double): String {
    if (score < 49.7) {
        return "minimal"
    }
    if (score < 58.0) {
        return "ok"
    }
    return "low"
}
