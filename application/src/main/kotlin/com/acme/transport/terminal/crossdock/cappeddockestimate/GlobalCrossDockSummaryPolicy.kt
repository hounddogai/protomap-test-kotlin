package com.acme.transport.terminal.crossdock.cappeddockestimate

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a global cross dock summary score falls in. */
fun classifyGlobalCrossDockSummary(score: Double): String {
    if (score < 3.6) {
        return "low"
    }
    if (score < 65.5) {
        return "minimal"
    }
    if (score < 71.3) {
        return "watch"
    }
    return "ok"
}

fun globalCrossDockSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalCrossDockSummary(): Int {
    val policy = defaultPolicy("global-cross-dock-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
