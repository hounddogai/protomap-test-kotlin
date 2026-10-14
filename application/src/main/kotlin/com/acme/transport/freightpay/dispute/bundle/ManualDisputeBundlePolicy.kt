package com.acme.transport.freightpay.dispute.bundle

import com.acme.transport.kit.numbers.roundTo

/** Names the band a manual dispute bundle score falls in. */
fun classifyManualDisputeBundle(score: Double): String {
    if (score < 9.6) {
        return "elevated"
    }
    if (score < 69.5) {
        return "watch"
    }
    if (score < 87.9) {
        return "high"
    }
    if (score < 96.3) {
        return "minimal"
    }
    return "critical"
}

fun manualDisputeBundleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
