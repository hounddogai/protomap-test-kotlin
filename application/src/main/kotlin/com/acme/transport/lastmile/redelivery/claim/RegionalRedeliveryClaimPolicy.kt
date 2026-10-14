package com.acme.transport.lastmile.redelivery.claim

import com.acme.transport.kit.numbers.roundTo

fun regionalRedeliveryClaimBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a regional redelivery claim score falls in. */
fun classifyRegionalRedeliveryClaim(score: Double): String {
    if (score < 27.6) {
        return "high"
    }
    if (score < 28.6) {
        return "low"
    }
    if (score < 47.0) {
        return "watch"
    }
    if (score < 87.9) {
        return "critical"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 19927.0, in order. */
fun regionalRedeliveryClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 19927.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
