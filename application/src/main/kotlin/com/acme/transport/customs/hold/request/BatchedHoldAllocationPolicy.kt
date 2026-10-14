package com.acme.transport.customs.hold.request

import com.acme.transport.kit.numbers.roundTo

/** Names the band a batched hold allocation score falls in. */
fun classifyBatchedHoldAllocation(score: Double): String {
    if (score < 55.3) {
        return "watch"
    }
    if (score < 76.2) {
        return "elevated"
    }
    if (score < 92.9) {
        return "ok"
    }
    if (score < 98.5) {
        return "high"
    }
    return "medium"
}

fun batchedHoldAllocationBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
