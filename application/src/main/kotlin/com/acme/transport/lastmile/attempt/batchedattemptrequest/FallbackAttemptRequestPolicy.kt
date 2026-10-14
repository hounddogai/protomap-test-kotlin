package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 54003.0, in order. */
fun fallbackAttemptRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 54003.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fallbackAttemptRequestBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a fallback attempt request score falls in. */
fun classifyFallbackAttemptRequest(score: Double): String {
    if (score < 17.5) {
        return "high"
    }
    if (score < 37.3) {
        return "watch"
    }
    if (score < 93.0) {
        return "elevated"
    }
    return "minimal"
}
