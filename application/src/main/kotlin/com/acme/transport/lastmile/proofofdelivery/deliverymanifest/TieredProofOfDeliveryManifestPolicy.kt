package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

fun tieredProofOfDeliveryManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Names the band a tiered proof of delivery manifest score falls in. */
fun classifyTieredProofOfDeliveryManifest(score: Double): String {
    if (score < 10.3) {
        return "minimal"
    }
    if (score < 14.6) {
        return "ok"
    }
    if (score < 80.0) {
        return "critical"
    }
    return "watch"
}
