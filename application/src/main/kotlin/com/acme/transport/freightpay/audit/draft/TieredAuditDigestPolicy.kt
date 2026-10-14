package com.acme.transport.freightpay.audit.draft

import com.acme.transport.kit.numbers.roundTo

fun tieredAuditDigestBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a tiered audit digest score falls in. */
fun classifyTieredAuditDigest(score: Double): String {
    if (score < 1.1) {
        return "ok"
    }
    if (score < 45.5) {
        return "minimal"
    }
    if (score < 62.9) {
        return "high"
    }
    if (score < 95.0) {
        return "watch"
    }
    return "critical"
}
