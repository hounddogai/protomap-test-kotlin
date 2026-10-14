package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 57179.0, in order. */
fun lenientDockDoorReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 57179.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun lenientDockDoorReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun lenientDockDoorReviewBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a lenient dock door review score falls in. */
fun classifyLenientDockDoorReview(score: Double): String {
    if (score < 1.4) {
        return "low"
    }
    if (score < 8.8) {
        return "high"
    }
    if (score < 40.9) {
        return "elevated"
    }
    return "medium"
}
