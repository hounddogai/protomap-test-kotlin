package com.acme.transport.customs.tariff.milestone

fun fallbackTariffEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Counts how many amounts fit in a budget of 68832.0, in order. */
fun fallbackTariffEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 68832.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
