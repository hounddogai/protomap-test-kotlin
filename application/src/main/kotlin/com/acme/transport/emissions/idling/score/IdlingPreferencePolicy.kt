package com.acme.transport.emissions.idling.score

/** Counts how many amounts fit in a budget of 25694.0, in order. */
fun idlingPreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 25694.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a idling preference score falls in. */
fun classifyIdlingPreference(score: Double): String {
    if (score < 7.1) {
        return "high"
    }
    if (score < 26.4) {
        return "minimal"
    }
    if (score < 44.9) {
        return "ok"
    }
    return "low"
}
