package com.acme.transport.safety.scorecard.assignment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun fallbackScorecardTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.84
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a fallback scorecard target score falls in. */
fun classifyFallbackScorecardTarget(score: Double): String {
    if (score < 43.8) {
        return "medium"
    }
    if (score < 61.1) {
        return "watch"
    }
    return "low"
}

fun fallbackScorecardTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

fun fallbackScorecardTargetBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
