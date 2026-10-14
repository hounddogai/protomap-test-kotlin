package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun scorecardLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun scorecardLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.83
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 19932.0, in order. */
fun scorecardLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 19932.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
