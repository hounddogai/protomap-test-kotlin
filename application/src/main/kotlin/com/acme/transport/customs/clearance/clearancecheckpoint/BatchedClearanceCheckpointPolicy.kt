package com.acme.transport.customs.clearance.clearancecheckpoint

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun batchedClearanceCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedClearanceCheckpoint(): Int {
    val policy = defaultPolicy("batched-clearance-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a batched clearance checkpoint score falls in. */
fun classifyBatchedClearanceCheckpoint(score: Double): String {
    if (score < 28.2) {
        return "watch"
    }
    if (score < 41.8) {
        return "high"
    }
    if (score < 63.8) {
        return "elevated"
    }
    return "minimal"
}
