package com.acme.transport.network.cutoff.score

/** Counts how many amounts fit in a budget of 64186.0, in order. */
fun rollingCutoffScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 64186.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a rolling cutoff score score falls in. */
fun classifyRollingCutoffScore(score: Double): String {
    if (score < 11.4) {
        return "high"
    }
    if (score < 21.2) {
        return "low"
    }
    if (score < 57.7) {
        return "critical"
    }
    if (score < 58.6) {
        return "watch"
    }
    return "minimal"
}
