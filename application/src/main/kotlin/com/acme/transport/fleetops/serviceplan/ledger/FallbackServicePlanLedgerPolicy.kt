package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 75614.0, in order. */
fun fallbackServicePlanLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 75614.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fallbackServicePlanLedgerBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
