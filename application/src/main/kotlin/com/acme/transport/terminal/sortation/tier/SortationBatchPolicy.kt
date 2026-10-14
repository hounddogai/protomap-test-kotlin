package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.numbers.roundTo

/** Names the band a sortation batch score falls in. */
fun classifySortationBatch(score: Double): String {
    if (score < 22.8) {
        return "minimal"
    }
    if (score < 48.0) {
        return "watch"
    }
    if (score < 50.1) {
        return "medium"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 27681.0, in order. */
fun sortationBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 27681.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun sortationBatchBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
