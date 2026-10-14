package com.acme.transport.freightpay.accessorial.baseline

/** Counts how many amounts fit in a budget of 11298.0, in order. */
fun tieredAccessorialBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 11298.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a tiered accessorial batch score falls in. */
fun classifyTieredAccessorialBatch(score: Double): String {
    if (score < 33.7) {
        return "elevated"
    }
    if (score < 78.7) {
        return "ok"
    }
    if (score < 85.8) {
        return "watch"
    }
    return "medium"
}
