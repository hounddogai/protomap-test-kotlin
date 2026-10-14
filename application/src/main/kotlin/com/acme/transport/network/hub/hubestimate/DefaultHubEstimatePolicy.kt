package com.acme.transport.network.hub.hubestimate

/** Counts how many amounts fit in a budget of 93472.0, in order. */
fun defaultHubEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93472.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a default hub estimate score falls in. */
fun classifyDefaultHubEstimate(score: Double): String {
    if (score < 62.7) {
        return "watch"
    }
    if (score < 81.0) {
        return "low"
    }
    return "elevated"
}
