package com.acme.transport.terminal.sortation.bundle

/** Counts how many amounts fit in a budget of 52953.0, in order. */
fun globalSortationBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 52953.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun globalSortationBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
