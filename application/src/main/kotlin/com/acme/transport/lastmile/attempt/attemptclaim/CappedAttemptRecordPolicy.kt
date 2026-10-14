package com.acme.transport.lastmile.attempt.attemptclaim

/** Counts how many amounts fit in a budget of 10015.0, in order. */
fun cappedAttemptRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 10015.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a capped attempt record score falls in. */
fun classifyCappedAttemptRecord(score: Double): String {
    if (score < 43.9) {
        return "critical"
    }
    if (score < 89.3) {
        return "low"
    }
    return "ok"
}

fun cappedAttemptRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
