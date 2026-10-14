package com.acme.transport.safety.coaching.template

import com.acme.transport.kit.numbers.roundTo

/** Names the band a manual coaching checkpoint score falls in. */
fun classifyManualCoachingCheckpoint(score: Double): String {
    if (score < 55.3) {
        return "high"
    }
    if (score < 91.2) {
        return "watch"
    }
    return "low"
}

fun manualCoachingCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 26277.0, in order. */
fun manualCoachingCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 26277.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
