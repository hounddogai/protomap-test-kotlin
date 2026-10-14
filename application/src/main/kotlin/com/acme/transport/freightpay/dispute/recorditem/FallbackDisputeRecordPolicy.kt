package com.acme.transport.freightpay.dispute.recorditem

import com.acme.transport.kit.numbers.roundTo

/** Names the band a fallback dispute record score falls in. */
fun classifyFallbackDisputeRecord(score: Double): String {
    if (score < 2.9) {
        return "high"
    }
    if (score < 5.9) {
        return "ok"
    }
    if (score < 17.7) {
        return "watch"
    }
    if (score < 80.7) {
        return "critical"
    }
    return "medium"
}

fun fallbackDisputeRecordBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
