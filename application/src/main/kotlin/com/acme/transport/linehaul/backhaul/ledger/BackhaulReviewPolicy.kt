package com.acme.transport.linehaul.backhaul.ledger

import com.acme.transport.kit.numbers.roundTo

fun backhaulReviewBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a backhaul review score falls in. */
fun classifyBackhaulReview(score: Double): String {
    if (score < 13.3) {
        return "minimal"
    }
    if (score < 19.4) {
        return "watch"
    }
    if (score < 54.6) {
        return "elevated"
    }
    if (score < 86.4) {
        return "medium"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 47263.0, in order. */
fun backhaulReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 47263.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
