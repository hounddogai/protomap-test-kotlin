package com.acme.transport.emissions.routeoffset.estimate

/** Names the band a route offset estimate score falls in. */
fun classifyRouteOffsetEstimate(score: Double): String {
    if (score < 44.5) {
        return "elevated"
    }
    if (score < 53.5) {
        return "ok"
    }
    if (score < 88.0) {
        return "high"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 60413.0, in order. */
fun routeOffsetEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 60413.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
