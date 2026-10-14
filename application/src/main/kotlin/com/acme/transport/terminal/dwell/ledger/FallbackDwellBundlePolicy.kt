package com.acme.transport.terminal.dwell.ledger

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a fallback dwell bundle score falls in. */
fun classifyFallbackDwellBundle(score: Double): String {
    if (score < 2.2) {
        return "low"
    }
    if (score < 35.5) {
        return "high"
    }
    return "minimal"
}

fun fallbackDwellBundleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.96
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
