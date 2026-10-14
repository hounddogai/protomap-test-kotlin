package com.acme.transport.terminal.dockdoor.slot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 47866.0, in order. */
fun primaryDockDoorAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 47866.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun primaryDockDoorAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.9
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
