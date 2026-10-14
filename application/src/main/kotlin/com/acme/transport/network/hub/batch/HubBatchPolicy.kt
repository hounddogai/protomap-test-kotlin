package com.acme.transport.network.hub.batch

/** Names the band a hub batch score falls in. */
fun classifyHubBatch(score: Double): String {
    if (score < 4.9) {
        return "low"
    }
    if (score < 23.4) {
        return "watch"
    }
    if (score < 85.3) {
        return "medium"
    }
    return "minimal"
}

/** Counts how many amounts fit in a budget of 80042.0, in order. */
fun hubBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 80042.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun hubBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
