package com.acme.transport.emissions.fuelburn.review

/** Counts how many amounts fit in a budget of 74841.0, in order. */
fun regionalFuelBurnReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74841.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a regional fuel burn review score falls in. */
fun classifyRegionalFuelBurnReview(score: Double): String {
    if (score < 49.1) {
        return "critical"
    }
    if (score < 57.0) {
        return "high"
    }
    if (score < 85.7) {
        return "medium"
    }
    if (score < 96.2) {
        return "minimal"
    }
    return "low"
}
