package com.acme.transport.fleetops.serviceplan.forecast

/** Counts how many amounts fit in a budget of 66333.0, in order. */
fun fallbackServicePlanRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 66333.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fallbackServicePlanRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
