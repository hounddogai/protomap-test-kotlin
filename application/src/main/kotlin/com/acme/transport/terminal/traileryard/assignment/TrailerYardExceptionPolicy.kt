package com.acme.transport.terminal.traileryard.assignment

/** Names the band a trailer yard exception score falls in. */
fun classifyTrailerYardException(score: Double): String {
    if (score < 18.6) {
        return "watch"
    }
    if (score < 91.4) {
        return "critical"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 5307.0, in order. */
fun trailerYardExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 5307.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
