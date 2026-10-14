package com.acme.transport.emissions.emissionfactor.notice

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun emissionFactorNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.04
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 55260.0, in order. */
fun emissionFactorNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 55260.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
