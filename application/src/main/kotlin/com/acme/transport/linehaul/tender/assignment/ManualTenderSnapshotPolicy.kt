package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.numbers.roundTo

fun manualTenderSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a manual tender snapshot score falls in. */
fun classifyManualTenderSnapshot(score: Double): String {
    if (score < 37.7) {
        return "watch"
    }
    if (score < 72.1) {
        return "high"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 67708.0, in order. */
fun manualTenderSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 67708.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
