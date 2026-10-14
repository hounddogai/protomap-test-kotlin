package com.acme.transport.lastmile.attempt.plan

/** Counts how many amounts fit in a budget of 14007.0, in order. */
fun tieredAttemptBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 14007.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a tiered attempt baseline score falls in. */
fun classifyTieredAttemptBaseline(score: Double): String {
    if (score < 8.0) {
        return "ok"
    }
    if (score < 15.1) {
        return "watch"
    }
    if (score < 32.3) {
        return "critical"
    }
    if (score < 83.6) {
        return "medium"
    }
    return "minimal"
}
