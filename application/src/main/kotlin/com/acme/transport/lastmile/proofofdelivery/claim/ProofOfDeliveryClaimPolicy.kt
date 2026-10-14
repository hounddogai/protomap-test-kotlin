package com.acme.transport.lastmile.proofofdelivery.claim

/** Names the band a proof of delivery claim score falls in. */
fun classifyProofOfDeliveryClaim(score: Double): String {
    if (score < 40.4) {
        return "high"
    }
    if (score < 55.2) {
        return "low"
    }
    return "elevated"
}

fun proofOfDeliveryClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
