package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun adaptiveNearMissExceptionWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.99
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 92200.0, in order. */
fun adaptiveNearMissExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 92200.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
