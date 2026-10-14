package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun rollingLeaseReturnLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.96
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 98782.0, in order. */
fun rollingLeaseReturnLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 98782.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun rollingLeaseReturnLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
