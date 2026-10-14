package com.acme.transport.lastmile.proofofdelivery.budget

import com.acme.transport.kit.numbers.roundTo

fun proofOfDeliveryBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun proofOfDeliveryBaselineBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a proof of delivery baseline score falls in. */
fun classifyProofOfDeliveryBaseline(score: Double): String {
    if (score < 39.5) {
        return "ok"
    }
    if (score < 93.2) {
        return "critical"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 4268.0, in order. */
fun proofOfDeliveryBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 4268.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
