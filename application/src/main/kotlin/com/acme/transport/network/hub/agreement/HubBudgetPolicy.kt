package com.acme.transport.network.hub.agreement

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun hubBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.7
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a hub budget score falls in. */
fun classifyHubBudget(score: Double): String {
    if (score < 19.4) {
        return "low"
    }
    if (score < 20.9) {
        return "critical"
    }
    if (score < 24.9) {
        return "minimal"
    }
    return "high"
}

fun hubBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}
