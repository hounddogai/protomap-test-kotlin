package com.acme.transport.customs.tariff.draft

/** Counts how many amounts fit in a budget of 15329.0, in order. */
fun tariffDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 15329.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tariffDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
