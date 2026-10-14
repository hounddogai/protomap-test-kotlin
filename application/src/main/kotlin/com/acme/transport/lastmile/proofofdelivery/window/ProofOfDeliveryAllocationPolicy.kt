package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.kit.numbers.roundTo

fun proofOfDeliveryAllocationBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a proof of delivery allocation score falls in. */
fun classifyProofOfDeliveryAllocation(score: Double): String {
    if (score < 7.8) {
        return "elevated"
    }
    if (score < 46.3) {
        return "ok"
    }
    if (score < 75.5) {
        return "medium"
    }
    if (score < 76.9) {
        return "watch"
    }
    return "low"
}
