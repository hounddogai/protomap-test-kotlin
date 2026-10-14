package com.acme.transport.freightpay.fuelsurcharge.target

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 33705.0, in order. */
fun batchedFuelSurchargeRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 33705.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun batchedFuelSurchargeRequestBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a batched fuel surcharge request score falls in. */
fun classifyBatchedFuelSurchargeRequest(score: Double): String {
    if (score < 6.8) {
        return "elevated"
    }
    if (score < 15.2) {
        return "medium"
    }
    if (score < 87.0) {
        return "low"
    }
    return "minimal"
}
