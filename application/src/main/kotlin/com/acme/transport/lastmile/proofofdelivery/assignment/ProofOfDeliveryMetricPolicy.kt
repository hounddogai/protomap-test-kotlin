package com.acme.transport.lastmile.proofofdelivery.assignment

/** Counts how many amounts fit in a budget of 8923.0, in order. */
fun proofOfDeliveryMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8923.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun proofOfDeliveryMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
