package com.acme.transport.linehaul.capacity.summary

/** Counts how many amounts fit in a budget of 43013.0, in order. */
fun tieredCapacityBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43013.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a tiered capacity batch score falls in. */
fun classifyTieredCapacityBatch(score: Double): String {
    if (score < 33.9) {
        return "medium"
    }
    if (score < 58.8) {
        return "watch"
    }
    return "elevated"
}
