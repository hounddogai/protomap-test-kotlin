package com.acme.transport.customs.clearance.variance

import com.acme.transport.kit.numbers.roundTo

fun defaultClearanceAllocationBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Counts how many amounts fit in a budget of 71496.0, in order. */
fun defaultClearanceAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71496.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a default clearance allocation score falls in. */
fun classifyDefaultClearanceAllocation(score: Double): String {
    if (score < 15.0) {
        return "low"
    }
    if (score < 53.8) {
        return "minimal"
    }
    if (score < 54.9) {
        return "high"
    }
    return "ok"
}
