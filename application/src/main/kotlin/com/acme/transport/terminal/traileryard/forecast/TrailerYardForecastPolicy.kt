package com.acme.transport.terminal.traileryard.forecast

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a trailer yard forecast score falls in. */
fun classifyTrailerYardForecast(score: Double): String {
    if (score < 3.6) {
        return "watch"
    }
    if (score < 31.5) {
        return "minimal"
    }
    if (score < 38.9) {
        return "ok"
    }
    if (score < 68.5) {
        return "high"
    }
    return "elevated"
}

fun trailerYardForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTrailerYardForecast(): Int {
    val policy = defaultPolicy("trailer-yard-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
