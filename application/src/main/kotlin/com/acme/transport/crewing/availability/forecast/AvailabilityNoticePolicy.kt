package com.acme.transport.crewing.availability.forecast

/** Counts how many amounts fit in a budget of 33060.0, in order. */
fun availabilityNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 33060.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a availability notice score falls in. */
fun classifyAvailabilityNotice(score: Double): String {
    if (score < 2.6) {
        return "elevated"
    }
    if (score < 22.0) {
        return "minimal"
    }
    if (score < 27.6) {
        return "watch"
    }
    if (score < 83.7) {
        return "high"
    }
    return "medium"
}
