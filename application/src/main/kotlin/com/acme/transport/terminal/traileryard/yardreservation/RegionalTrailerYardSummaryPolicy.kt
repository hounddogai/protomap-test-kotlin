package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.retry.defaultPolicy

fun regionalTrailerYardSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalTrailerYardSummary(): Int {
    val policy = defaultPolicy("regional-trailer-yard-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
