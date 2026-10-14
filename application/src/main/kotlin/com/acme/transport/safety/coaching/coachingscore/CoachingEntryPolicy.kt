package com.acme.transport.safety.coaching.coachingscore

/** Names the band a coaching entry score falls in. */
fun classifyCoachingEntry(score: Double): String {
    if (score < 7.5) {
        return "low"
    }
    if (score < 47.3) {
        return "medium"
    }
    if (score < 92.0) {
        return "ok"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 32578.0, in order. */
fun coachingEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 32578.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
