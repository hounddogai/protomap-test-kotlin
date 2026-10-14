package com.acme.transport.network.servicearea.areacycle

/** Counts how many amounts fit in a budget of 25418.0, in order. */
fun batchedServiceAreaCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 25418.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a batched service area cycle score falls in. */
fun classifyBatchedServiceAreaCycle(score: Double): String {
    if (score < 41.0) {
        return "elevated"
    }
    if (score < 75.5) {
        return "critical"
    }
    if (score < 90.0) {
        return "watch"
    }
    return "ok"
}
