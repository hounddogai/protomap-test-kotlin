package com.acme.transport.freightpay.dispute.milestone

import com.acme.transport.kit.numbers.roundTo

fun disputeSegmentBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a dispute segment score falls in. */
fun classifyDisputeSegment(score: Double): String {
    if (score < 83.0) {
        return "critical"
    }
    if (score < 93.1) {
        return "high"
    }
    return "elevated"
}
