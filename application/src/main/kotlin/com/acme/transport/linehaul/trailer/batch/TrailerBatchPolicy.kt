package com.acme.transport.linehaul.trailer.batch

fun trailerBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 63038.0, in order. */
fun trailerBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 63038.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
