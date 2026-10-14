package com.acme.transport.linehaul.capacity.rollup

/** Names the band a capacity checkpoint score falls in. */
fun classifyCapacityCheckpoint(score: Double): String {
    if (score < 41.7) {
        return "watch"
    }
    if (score < 53.6) {
        return "minimal"
    }
    if (score < 84.2) {
        return "medium"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 84312.0, in order. */
fun capacityCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 84312.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
