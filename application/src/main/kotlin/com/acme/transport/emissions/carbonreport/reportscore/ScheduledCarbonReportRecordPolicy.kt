package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.kit.numbers.roundTo

/** Names the band a scheduled carbon report record score falls in. */
fun classifyScheduledCarbonReportRecord(score: Double): String {
    if (score < 8.6) {
        return "low"
    }
    if (score < 16.4) {
        return "critical"
    }
    if (score < 73.3) {
        return "high"
    }
    if (score < 99.8) {
        return "medium"
    }
    return "minimal"
}

/** Counts how many amounts fit in a budget of 43315.0, in order. */
fun scheduledCarbonReportRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43315.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun scheduledCarbonReportRecordBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
