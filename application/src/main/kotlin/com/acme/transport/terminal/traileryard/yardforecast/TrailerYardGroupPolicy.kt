package com.acme.transport.terminal.traileryard.yardforecast

/** Names the band a trailer yard group score falls in. */
fun classifyTrailerYardGroup(score: Double): String {
    if (score < 8.1) {
        return "high"
    }
    if (score < 22.7) {
        return "minimal"
    }
    if (score < 30.5) {
        return "low"
    }
    if (score < 88.4) {
        return "ok"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 15903.0, in order. */
fun trailerYardGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 15903.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
