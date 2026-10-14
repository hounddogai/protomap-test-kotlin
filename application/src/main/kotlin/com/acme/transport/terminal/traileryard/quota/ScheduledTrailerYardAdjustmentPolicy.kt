package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledTrailerYardAdjustment(): Int {
    val policy = defaultPolicy("scheduled-trailer-yard-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun scheduledTrailerYardAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a scheduled trailer yard adjustment score falls in. */
fun classifyScheduledTrailerYardAdjustment(score: Double): String {
    if (score < 79.3) {
        return "low"
    }
    if (score < 83.3) {
        return "ok"
    }
    return "minimal"
}
