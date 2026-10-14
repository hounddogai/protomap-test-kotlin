package com.acme.transport.safety.incident.claim

import com.acme.transport.kit.numbers.roundTo

/** Names the band a regional incident review score falls in. */
fun classifyRegionalIncidentReview(score: Double): String {
    if (score < 6.6) {
        return "elevated"
    }
    if (score < 62.3) {
        return "high"
    }
    return "medium"
}

fun regionalIncidentReviewBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 95115.0, in order. */
fun regionalIncidentReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 95115.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
