package com.acme.transport.terminal.dwell.plan

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun regionalDwellPlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.37
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a regional dwell plan score falls in. */
fun classifyRegionalDwellPlan(score: Double): String {
    if (score < 16.6) {
        return "elevated"
    }
    if (score < 49.6) {
        return "critical"
    }
    if (score < 59.4) {
        return "medium"
    }
    if (score < 84.9) {
        return "minimal"
    }
    return "watch"
}

fun regionalDwellPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
