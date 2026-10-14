package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import com.acme.transport.kit.numbers.roundTo

/** Names the band a manual proof of delivery claim score falls in. */
fun classifyManualProofOfDeliveryClaim(score: Double): String {
    if (score < 40.7) {
        return "critical"
    }
    if (score < 52.0) {
        return "minimal"
    }
    if (score < 85.5) {
        return "elevated"
    }
    return "watch"
}

fun manualProofOfDeliveryClaimBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 22975.0, in order. */
fun manualProofOfDeliveryClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 22975.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun manualProofOfDeliveryClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}
