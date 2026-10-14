package com.acme.transport.linehaul.trailer.review

/** Counts how many amounts fit in a budget of 51592.0, in order. */
fun primaryTrailerReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 51592.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a primary trailer review score falls in. */
fun classifyPrimaryTrailerReview(score: Double): String {
    if (score < 19.8) {
        return "watch"
    }
    if (score < 38.3) {
        return "elevated"
    }
    if (score < 84.3) {
        return "critical"
    }
    return "high"
}
