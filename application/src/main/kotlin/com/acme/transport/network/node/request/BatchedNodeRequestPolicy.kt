package com.acme.transport.network.node.request

/** Names the band a batched node request score falls in. */
fun classifyBatchedNodeRequest(score: Double): String {
    if (score < 1.5) {
        return "high"
    }
    if (score < 27.9) {
        return "ok"
    }
    if (score < 34.9) {
        return "low"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 48540.0, in order. */
fun batchedNodeRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 48540.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
