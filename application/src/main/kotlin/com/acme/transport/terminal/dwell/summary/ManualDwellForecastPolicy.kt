package com.acme.transport.terminal.dwell.summary

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a manual dwell forecast score falls in. */
fun classifyManualDwellForecast(score: Double): String {
    if (score < 48.3) {
        return "medium"
    }
    if (score < 82.6) {
        return "minimal"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualDwellForecast(): Int {
    val policy = defaultPolicy("manual-dwell-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun manualDwellForecastBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
