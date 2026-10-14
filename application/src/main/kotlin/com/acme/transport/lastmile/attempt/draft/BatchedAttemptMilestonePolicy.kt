package com.acme.transport.lastmile.attempt.draft

import com.acme.transport.kit.numbers.roundTo

fun batchedAttemptMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a batched attempt milestone score falls in. */
fun classifyBatchedAttemptMilestone(score: Double): String {
    if (score < 18.2) {
        return "medium"
    }
    if (score < 19.3) {
        return "high"
    }
    return "elevated"
}
