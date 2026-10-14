package com.acme.transport.customs.broker.metric

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 35042.0, in order. */
fun batchedBrokerClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 35042.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun batchedBrokerClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.51
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
