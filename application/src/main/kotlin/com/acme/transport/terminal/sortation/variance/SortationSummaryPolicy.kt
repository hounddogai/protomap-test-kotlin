package com.acme.transport.terminal.sortation.variance

import com.acme.transport.kit.numbers.roundTo

fun sortationSummaryBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 59480.0, in order. */
fun sortationSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 59480.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun sortationSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Names the band a sortation summary score falls in. */
fun classifySortationSummary(score: Double): String {
    if (score < 42.6) {
        return "critical"
    }
    if (score < 45.7) {
        return "low"
    }
    return "minimal"
}
