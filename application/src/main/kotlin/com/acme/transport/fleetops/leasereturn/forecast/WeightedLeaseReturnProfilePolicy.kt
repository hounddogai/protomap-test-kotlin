package com.acme.transport.fleetops.leasereturn.forecast

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a weighted lease return profile score falls in. */
fun classifyWeightedLeaseReturnProfile(score: Double): String {
    if (score < 7.6) {
        return "elevated"
    }
    if (score < 13.6) {
        return "ok"
    }
    if (score < 74.6) {
        return "watch"
    }
    if (score < 97.3) {
        return "medium"
    }
    return "low"
}

fun weightedLeaseReturnProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun weightedLeaseReturnProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 94570.0, in order. */
fun weightedLeaseReturnProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 94570.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
