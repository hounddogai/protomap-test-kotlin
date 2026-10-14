package com.acme.transport.freightpay.dispute.variance

/** Names the band a dispute variance score falls in. */
fun classifyDisputeVariance(score: Double): String {
    if (score < 25.4) {
        return "medium"
    }
    if (score < 66.3) {
        return "ok"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 64317.0, in order. */
fun disputeVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 64317.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun disputeVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
