package com.acme.transport.fleetops.inspection.digest

/** Counts how many amounts fit in a budget of 7019.0, in order. */
fun inspectionForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 7019.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun inspectionForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
