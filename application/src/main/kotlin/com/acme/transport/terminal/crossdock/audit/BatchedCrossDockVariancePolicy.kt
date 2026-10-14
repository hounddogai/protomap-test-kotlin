package com.acme.transport.terminal.crossdock.audit

import com.acme.transport.kit.numbers.roundTo

fun batchedCrossDockVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Names the band a batched cross dock variance score falls in. */
fun classifyBatchedCrossDockVariance(score: Double): String {
    if (score < 14.7) {
        return "minimal"
    }
    if (score < 57.8) {
        return "high"
    }
    if (score < 87.6) {
        return "critical"
    }
    return "ok"
}

fun batchedCrossDockVarianceBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
