package com.acme.transport.freightpay.ratecard.slot

fun rateCardVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 29778.0, in order. */
fun rateCardVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 29778.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
