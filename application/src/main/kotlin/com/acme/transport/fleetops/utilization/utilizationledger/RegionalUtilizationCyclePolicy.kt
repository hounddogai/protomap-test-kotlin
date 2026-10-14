package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun regionalUtilizationCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun regionalUtilizationCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.72
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a regional utilization cycle score falls in. */
fun classifyRegionalUtilizationCycle(score: Double): String {
    if (score < 12.8) {
        return "critical"
    }
    if (score < 73.3) {
        return "minimal"
    }
    return "medium"
}
