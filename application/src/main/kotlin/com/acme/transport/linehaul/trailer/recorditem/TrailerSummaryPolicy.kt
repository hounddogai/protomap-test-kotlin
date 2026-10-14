package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a trailer summary score falls in. */
fun classifyTrailerSummary(score: Double): String {
    if (score < 13.2) {
        return "high"
    }
    if (score < 28.3) {
        return "medium"
    }
    if (score < 59.4) {
        return "ok"
    }
    return "low"
}

fun trailerSummaryBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTrailerSummary(): Int {
    val policy = defaultPolicy("trailer-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
