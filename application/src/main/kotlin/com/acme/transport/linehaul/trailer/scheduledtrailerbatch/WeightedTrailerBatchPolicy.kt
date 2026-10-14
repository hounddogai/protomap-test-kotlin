package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 86652.0, in order. */
fun weightedTrailerBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 86652.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun weightedTrailerBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
