package com.acme.transport.linehaul.trailer.claim

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun trailerAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.72
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 83546.0, in order. */
fun trailerAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 83546.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
