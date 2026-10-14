package com.acme.transport.crewing.dutyperiod.request

/** Counts how many amounts fit in a budget of 69895.0, in order. */
fun dutyPeriodRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 69895.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun dutyPeriodRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
