package com.acme.transport.emissions.fuelburn.allocation

/** Names the band a rolling fuel burn record score falls in. */
fun classifyRollingFuelBurnRecord(score: Double): String {
    if (score < 6.7) {
        return "minimal"
    }
    if (score < 14.4) {
        return "low"
    }
    if (score < 63.5) {
        return "watch"
    }
    if (score < 64.7) {
        return "medium"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 95102.0, in order. */
fun rollingFuelBurnRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 95102.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun rollingFuelBurnRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
