package com.acme.transport.network.cutoff.score

import com.acme.transport.kit.numbers.roundTo

fun lenientCutoffAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a lenient cutoff assignment score falls in. */
fun classifyLenientCutoffAssignment(score: Double): String {
    if (score < 78.9) {
        return "watch"
    }
    if (score < 85.1) {
        return "ok"
    }
    return "high"
}
