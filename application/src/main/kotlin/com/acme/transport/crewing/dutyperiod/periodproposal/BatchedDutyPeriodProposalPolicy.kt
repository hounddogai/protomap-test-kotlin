package com.acme.transport.crewing.dutyperiod.periodproposal

import com.acme.transport.kit.numbers.roundTo

fun batchedDutyPeriodProposalBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a batched duty period proposal score falls in. */
fun classifyBatchedDutyPeriodProposal(score: Double): String {
    if (score < 15.0) {
        return "high"
    }
    if (score < 20.6) {
        return "low"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 71573.0, in order. */
fun batchedDutyPeriodProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71573.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
