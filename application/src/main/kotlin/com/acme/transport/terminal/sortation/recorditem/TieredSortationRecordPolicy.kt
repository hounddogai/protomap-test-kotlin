package com.acme.transport.terminal.sortation.recorditem

/** Counts how many amounts fit in a budget of 66856.0, in order. */
fun tieredSortationRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 66856.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a tiered sortation record score falls in. */
fun classifyTieredSortationRecord(score: Double): String {
    if (score < 9.4) {
        return "medium"
    }
    if (score < 86.2) {
        return "high"
    }
    return "low"
}

fun tieredSortationRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
