package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

fun batchedProofOfDeliveryPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Names the band a batched proof of delivery policy score falls in. */
fun classifyBatchedProofOfDeliveryPolicy(score: Double): String {
    if (score < 60.2) {
        return "watch"
    }
    if (score < 68.0) {
        return "high"
    }
    if (score < 88.5) {
        return "medium"
    }
    return "ok"
}
