package com.acme.transport.terminal.crossdock.dockclaim

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun crossDockDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.52
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 10119.0, in order. */
fun crossDockDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 10119.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
