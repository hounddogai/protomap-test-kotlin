package com.acme.transport.network.hub.mapping

/** Counts how many amounts fit in a budget of 8362.0, in order. */
fun hubQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8362.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a hub quota score falls in. */
fun classifyHubQuota(score: Double): String {
    if (score < 14.2) {
        return "medium"
    }
    if (score < 66.6) {
        return "elevated"
    }
    if (score < 86.9) {
        return "minimal"
    }
    if (score < 98.6) {
        return "high"
    }
    return "critical"
}
