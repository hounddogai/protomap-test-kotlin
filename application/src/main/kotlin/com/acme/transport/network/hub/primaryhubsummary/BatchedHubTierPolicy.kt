package com.acme.transport.network.hub.primaryhubsummary

import com.acme.transport.kit.numbers.roundTo

/** Names the band a batched hub tier score falls in. */
fun classifyBatchedHubTier(score: Double): String {
    if (score < 14.3) {
        return "critical"
    }
    if (score < 58.9) {
        return "minimal"
    }
    if (score < 89.7) {
        return "watch"
    }
    if (score < 98.9) {
        return "low"
    }
    return "ok"
}

fun batchedHubTierBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
