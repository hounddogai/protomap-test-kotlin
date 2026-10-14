package com.acme.transport.freightpay.audit.cycle

/** Counts how many amounts fit in a budget of 97574.0, in order. */
fun manualAuditOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 97574.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun manualAuditOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}
