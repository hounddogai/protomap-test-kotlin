package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 25131.0, in order. */
fun lenientTenderCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 25131.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun lenientTenderCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun lenientTenderCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Names the band a lenient tender checkpoint score falls in. */
fun classifyLenientTenderCheckpoint(score: Double): String {
    if (score < 42.1) {
        return "medium"
    }
    if (score < 84.3) {
        return "elevated"
    }
    if (score < 97.0) {
        return "watch"
    }
    return "ok"
}
