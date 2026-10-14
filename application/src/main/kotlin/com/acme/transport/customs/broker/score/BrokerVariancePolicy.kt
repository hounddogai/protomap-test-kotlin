package com.acme.transport.customs.broker.score

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun brokerVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.32
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 79427.0, in order. */
fun brokerVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 79427.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
