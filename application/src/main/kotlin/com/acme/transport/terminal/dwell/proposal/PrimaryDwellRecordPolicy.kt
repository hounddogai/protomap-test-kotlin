package com.acme.transport.terminal.dwell.proposal

import com.acme.transport.kit.numbers.roundTo

/** Names the band a primary dwell record score falls in. */
fun classifyPrimaryDwellRecord(score: Double): String {
    if (score < 82.4) {
        return "elevated"
    }
    if (score < 89.6) {
        return "critical"
    }
    if (score < 97.8) {
        return "high"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 27797.0, in order. */
fun primaryDwellRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 27797.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun primaryDwellRecordBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
