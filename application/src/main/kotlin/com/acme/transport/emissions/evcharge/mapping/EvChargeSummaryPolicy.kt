package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.kit.numbers.roundTo

/** Names the band a ev charge summary score falls in. */
fun classifyEvChargeSummary(score: Double): String {
    if (score < 3.9) {
        return "medium"
    }
    if (score < 33.4) {
        return "watch"
    }
    if (score < 87.4) {
        return "elevated"
    }
    return "critical"
}

fun evChargeSummaryBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 13418.0, in order. */
fun evChargeSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 13418.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
