package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 49349.0, in order. */
fun attemptReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 49349.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun attemptReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.57
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
