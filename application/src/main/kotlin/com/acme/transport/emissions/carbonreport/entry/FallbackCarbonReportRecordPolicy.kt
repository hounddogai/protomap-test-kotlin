package com.acme.transport.emissions.carbonreport.entry

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 2468.0, in order. */
fun fallbackCarbonReportRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2468.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fallbackCarbonReportRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

fun fallbackCarbonReportRecordBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a fallback carbon report record score falls in. */
fun classifyFallbackCarbonReportRecord(score: Double): String {
    if (score < 60.3) {
        return "critical"
    }
    if (score < 84.4) {
        return "watch"
    }
    if (score < 99.9) {
        return "medium"
    }
    return "low"
}
