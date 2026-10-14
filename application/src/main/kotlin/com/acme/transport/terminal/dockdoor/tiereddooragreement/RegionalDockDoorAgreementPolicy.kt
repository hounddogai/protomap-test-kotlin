package com.acme.transport.terminal.dockdoor.tiereddooragreement

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun regionalDockDoorAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.16
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun regionalDockDoorAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Counts how many amounts fit in a budget of 8154.0, in order. */
fun regionalDockDoorAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8154.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
