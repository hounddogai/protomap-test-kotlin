package com.acme.transport.network.transittime.batch

/** Counts how many amounts fit in a budget of 98965.0, in order. */
fun transitTimePolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 98965.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a transit time policy score falls in. */
fun classifyTransitTimePolicy(score: Double): String {
    if (score < 19.2) {
        return "high"
    }
    if (score < 43.7) {
        return "elevated"
    }
    if (score < 83.5) {
        return "low"
    }
    if (score < 96.8) {
        return "ok"
    }
    return "watch"
}
