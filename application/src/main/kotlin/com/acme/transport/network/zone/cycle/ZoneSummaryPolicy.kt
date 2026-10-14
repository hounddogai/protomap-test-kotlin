package com.acme.transport.network.zone.cycle

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun zoneSummaryBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a zone summary score falls in. */
fun classifyZoneSummary(score: Double): String {
    if (score < 1.1) {
        return "ok"
    }
    if (score < 39.4) {
        return "watch"
    }
    if (score < 73.2) {
        return "elevated"
    }
    return "high"
}

fun zoneSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeZoneSummary(): Int {
    val policy = defaultPolicy("zone-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
