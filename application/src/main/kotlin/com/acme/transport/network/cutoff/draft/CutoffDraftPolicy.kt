package com.acme.transport.network.cutoff.draft

import com.acme.transport.kit.numbers.roundTo

/** Names the band a cutoff draft score falls in. */
fun classifyCutoffDraft(score: Double): String {
    if (score < 11.4) {
        return "minimal"
    }
    if (score < 64.3) {
        return "ok"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 49351.0, in order. */
fun cutoffDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 49351.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun cutoffDraftBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
