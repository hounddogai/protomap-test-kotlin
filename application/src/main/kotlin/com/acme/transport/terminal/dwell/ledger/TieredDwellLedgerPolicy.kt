package com.acme.transport.terminal.dwell.ledger

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 31608.0, in order. */
fun tieredDwellLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 31608.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tieredDwellLedgerBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun tieredDwellLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
