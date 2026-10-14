package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.kit.numbers.roundTo

fun proofOfDeliveryRollupBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun proofOfDeliveryRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a proof of delivery rollup score falls in. */
fun classifyProofOfDeliveryRollup(score: Double): String {
    if (score < 18.3) {
        return "high"
    }
    if (score < 37.4) {
        return "elevated"
    }
    if (score < 80.3) {
        return "minimal"
    }
    if (score < 97.9) {
        return "medium"
    }
    return "watch"
}
