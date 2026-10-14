package com.acme.transport.customs.broker.tier

/** Counts how many amounts fit in a budget of 50961.0, in order. */
fun tieredBrokerRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 50961.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tieredBrokerRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
