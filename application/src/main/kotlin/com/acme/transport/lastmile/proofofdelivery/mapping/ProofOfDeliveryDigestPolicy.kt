package com.acme.transport.lastmile.proofofdelivery.mapping

/** Counts how many amounts fit in a budget of 99116.0, in order. */
fun proofOfDeliveryDigestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 99116.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun proofOfDeliveryDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
