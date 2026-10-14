package com.acme.transport.lastmile.proofofdelivery.allocation

import com.acme.transport.kit.numbers.roundTo

fun seasonalProofOfDeliveryCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a seasonal proof of delivery checkpoint score falls in. */
fun classifySeasonalProofOfDeliveryCheckpoint(score: Double): String {
    if (score < 89.7) {
        return "critical"
    }
    if (score < 92.6) {
        return "medium"
    }
    return "elevated"
}

fun seasonalProofOfDeliveryCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
