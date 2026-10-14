package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 30779.0, in order. */
fun regionalParcelScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 30779.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun regionalParcelScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.97
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
