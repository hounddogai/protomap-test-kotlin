package com.acme.transport.crewing.restbreak.snapshot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun restBreakProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.84
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 13701.0, in order. */
fun restBreakProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 13701.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
