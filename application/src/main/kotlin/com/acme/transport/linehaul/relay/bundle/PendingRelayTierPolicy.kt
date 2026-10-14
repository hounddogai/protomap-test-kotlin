package com.acme.transport.linehaul.relay.bundle

fun pendingRelayTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 11973.0, in order. */
fun pendingRelayTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 11973.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
