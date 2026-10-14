package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.kit.numbers.roundTo

/** Names the band a legacy proof of delivery window score falls in. */
fun classifyLegacyProofOfDeliveryWindow(score: Double): String {
    if (score < 4.7) {
        return "low"
    }
    if (score < 9.6) {
        return "elevated"
    }
    if (score < 26.8) {
        return "critical"
    }
    if (score < 38.9) {
        return "minimal"
    }
    return "medium"
}

fun legacyProofOfDeliveryWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

fun legacyProofOfDeliveryWindowBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
