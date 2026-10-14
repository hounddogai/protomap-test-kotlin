package com.acme.transport.emissions.carbonreport.agreement

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 8605.0, in order. */
fun carbonReportRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8605.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a carbon report record score falls in. */
fun classifyCarbonReportRecord(score: Double): String {
    if (score < 59.6) {
        return "elevated"
    }
    if (score < 75.6) {
        return "watch"
    }
    if (score < 96.0) {
        return "low"
    }
    return "critical"
}

fun carbonReportRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun carbonReportRecordBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
