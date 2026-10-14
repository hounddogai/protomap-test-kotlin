package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 53987.0, in order. */
fun weightedRateCardQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 53987.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun weightedRateCardQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.8
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
