package com.acme.transport.freightpay.dispute.template

import com.acme.transport.kit.numbers.roundTo

fun disputeRollupBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a dispute rollup score falls in. */
fun classifyDisputeRollup(score: Double): String {
    if (score < 7.4) {
        return "high"
    }
    if (score < 61.6) {
        return "medium"
    }
    return "watch"
}
