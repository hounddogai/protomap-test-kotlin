package com.acme.transport.freightpay.dispute.batcheddisputepolicy

/** Names the band a lenient dispute policy score falls in. */
fun classifyLenientDisputePolicy(score: Double): String {
    if (score < 16.3) {
        return "ok"
    }
    if (score < 86.7) {
        return "minimal"
    }
    if (score < 89.9) {
        return "low"
    }
    return "watch"
}

fun lenientDisputePolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 71396.0, in order. */
fun lenientDisputePolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71396.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
