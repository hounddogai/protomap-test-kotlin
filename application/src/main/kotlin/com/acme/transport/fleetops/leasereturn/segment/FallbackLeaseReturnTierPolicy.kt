package com.acme.transport.fleetops.leasereturn.segment

import com.acme.transport.kit.numbers.roundTo

/** Names the band a fallback lease return tier score falls in. */
fun classifyFallbackLeaseReturnTier(score: Double): String {
    if (score < 34.9) {
        return "medium"
    }
    if (score < 61.5) {
        return "watch"
    }
    if (score < 65.8) {
        return "minimal"
    }
    return "critical"
}

fun fallbackLeaseReturnTierBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun fallbackLeaseReturnTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
