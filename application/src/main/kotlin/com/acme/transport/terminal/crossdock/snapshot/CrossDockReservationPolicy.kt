package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 73950.0, in order. */
fun crossDockReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 73950.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun crossDockReservationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.24
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
