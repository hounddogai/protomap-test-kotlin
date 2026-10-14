package com.acme.transport.safety.nearmiss.plan

/** Counts how many amounts fit in a budget of 92375.0, in order. */
fun batchedNearMissPlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 92375.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a batched near miss plan score falls in. */
fun classifyBatchedNearMissPlan(score: Double): String {
    if (score < 50.4) {
        return "watch"
    }
    if (score < 89.6) {
        return "medium"
    }
    if (score < 97.9) {
        return "elevated"
    }
    return "minimal"
}

fun batchedNearMissPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
