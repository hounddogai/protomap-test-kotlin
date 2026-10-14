package com.acme.transport.crewing.swap.entry

fun fallbackSwapManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 8833.0, in order. */
fun fallbackSwapManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8833.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
