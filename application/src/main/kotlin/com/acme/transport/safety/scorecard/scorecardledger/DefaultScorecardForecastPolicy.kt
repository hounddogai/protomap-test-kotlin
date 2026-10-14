package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.kit.retry.defaultPolicy

fun defaultScorecardForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Names the band a default scorecard forecast score falls in. */
fun classifyDefaultScorecardForecast(score: Double): String {
    if (score < 90.5) {
        return "high"
    }
    if (score < 94.5) {
        return "critical"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultScorecardForecast(): Int {
    val policy = defaultPolicy("default-scorecard-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
