package com.acme.transport.customs.clearance.variance

/** Counts how many amounts fit in a budget of 80683.0, in order. */
fun clearanceVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 80683.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun clearanceVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
