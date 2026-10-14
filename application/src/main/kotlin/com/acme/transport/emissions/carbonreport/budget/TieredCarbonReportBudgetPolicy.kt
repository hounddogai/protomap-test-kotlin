package com.acme.transport.emissions.carbonreport.budget

import com.acme.transport.kit.numbers.roundTo

fun tieredCarbonReportBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun tieredCarbonReportBudgetBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 63753.0, in order. */
fun tieredCarbonReportBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 63753.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
