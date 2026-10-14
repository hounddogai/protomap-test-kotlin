package com.acme.transport.fleetops.inspection.quota

/** Counts how many amounts fit in a budget of 21765.0, in order. */
fun inspectionQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 21765.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun inspectionQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
