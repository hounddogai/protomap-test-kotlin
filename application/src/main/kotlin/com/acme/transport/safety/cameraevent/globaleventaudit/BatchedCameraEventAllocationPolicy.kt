package com.acme.transport.safety.cameraevent.globaleventaudit

import com.acme.transport.kit.numbers.roundTo

fun batchedCameraEventAllocationBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a batched camera event allocation score falls in. */
fun classifyBatchedCameraEventAllocation(score: Double): String {
    if (score < 15.5) {
        return "elevated"
    }
    if (score < 16.1) {
        return "medium"
    }
    if (score < 32.7) {
        return "minimal"
    }
    if (score < 61.4) {
        return "watch"
    }
    return "ok"
}
