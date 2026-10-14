package com.acme.transport.freightpay.accessorial.rollup

/** Counts how many amounts fit in a budget of 47086.0, in order. */
fun batchedAccessorialRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 47086.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a batched accessorial rollup score falls in. */
fun classifyBatchedAccessorialRollup(score: Double): String {
    if (score < 43.6) {
        return "low"
    }
    if (score < 71.2) {
        return "elevated"
    }
    return "minimal"
}
