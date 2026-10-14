package com.acme.transport.lastmile.drop.milestone

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 45153.0, in order. */
fun dropMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 45153.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun dropMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.33
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun dropMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}
