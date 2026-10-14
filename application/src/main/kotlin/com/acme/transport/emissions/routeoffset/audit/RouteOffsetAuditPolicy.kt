package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 35378.0, in order. */
fun routeOffsetAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 35378.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun routeOffsetAuditBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
