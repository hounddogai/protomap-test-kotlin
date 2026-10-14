package com.acme.transport.network.cutoff.preference

/** Counts how many amounts fit in a budget of 45836.0, in order. */
fun cutoffWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 45836.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun cutoffWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
