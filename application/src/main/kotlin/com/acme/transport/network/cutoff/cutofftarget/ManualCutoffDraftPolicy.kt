package com.acme.transport.network.cutoff.cutofftarget

fun manualCutoffDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Counts how many amounts fit in a budget of 24626.0, in order. */
fun manualCutoffDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24626.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
