package com.acme.transport.freightpay.dispute.reservation

/** Counts how many amounts fit in a budget of 15838.0, in order. */
fun legacyDisputeTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 15838.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun legacyDisputeTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
