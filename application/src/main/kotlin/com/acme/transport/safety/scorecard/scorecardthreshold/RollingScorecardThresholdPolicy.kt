package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 4078.0, in order. */
fun rollingScorecardThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 4078.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a rolling scorecard threshold score falls in. */
fun classifyRollingScorecardThreshold(score: Double): String {
    if (score < 24.2) {
        return "high"
    }
    if (score < 64.1) {
        return "critical"
    }
    if (score < 76.4) {
        return "minimal"
    }
    return "elevated"
}

fun rollingScorecardThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.62
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun rollingScorecardThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
