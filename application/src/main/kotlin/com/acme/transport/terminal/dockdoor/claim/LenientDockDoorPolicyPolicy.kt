package com.acme.transport.terminal.dockdoor.claim

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 38542.0, in order. */
fun lenientDockDoorPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 38542.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun lenientDockDoorPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.78
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
