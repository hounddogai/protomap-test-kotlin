package com.acme.transport.freightpay.dispute.digest

/** Counts how many amounts fit in a budget of 76013.0, in order. */
fun disputeAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76013.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a dispute adjustment score falls in. */
fun classifyDisputeAdjustment(score: Double): String {
    if (score < 79.7) {
        return "critical"
    }
    if (score < 81.9) {
        return "low"
    }
    return "watch"
}
