package com.acme.transport.safety.scorecard.scorecardgroup

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun batchedScorecardMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.05
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun batchedScorecardMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 70704.0, in order. */
fun batchedScorecardMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 70704.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
