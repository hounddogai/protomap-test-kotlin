package com.acme.transport.network.hub.milestone

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 43746.0, in order. */
fun hubVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43746.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun hubVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun hubVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.23
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
