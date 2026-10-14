package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.numbers.roundTo

/** Names the band a tiered proof of delivery score score falls in. */
fun classifyTieredProofOfDeliveryScore(score: Double): String {
    if (score < 16.7) {
        return "low"
    }
    if (score < 24.0) {
        return "ok"
    }
    if (score < 26.6) {
        return "elevated"
    }
    if (score < 69.8) {
        return "minimal"
    }
    return "critical"
}

fun tieredProofOfDeliveryScoreBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
