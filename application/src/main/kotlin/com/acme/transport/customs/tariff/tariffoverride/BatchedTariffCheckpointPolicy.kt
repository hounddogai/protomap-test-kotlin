package com.acme.transport.customs.tariff.tariffoverride

import com.acme.transport.kit.numbers.roundTo

/** Names the band a batched tariff checkpoint score falls in. */
fun classifyBatchedTariffCheckpoint(score: Double): String {
    if (score < 34.8) {
        return "elevated"
    }
    if (score < 42.8) {
        return "minimal"
    }
    if (score < 49.2) {
        return "medium"
    }
    if (score < 49.7) {
        return "high"
    }
    return "low"
}

fun batchedTariffCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun batchedTariffCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
