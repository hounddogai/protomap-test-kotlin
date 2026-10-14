package com.acme.transport.terminal.dockdoor.doorestimate

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun fallbackDockDoorEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.13
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a fallback dock door estimate score falls in. */
fun classifyFallbackDockDoorEstimate(score: Double): String {
    if (score < 18.5) {
        return "critical"
    }
    if (score < 23.3) {
        return "high"
    }
    if (score < 63.3) {
        return "watch"
    }
    if (score < 91.6) {
        return "ok"
    }
    return "low"
}
