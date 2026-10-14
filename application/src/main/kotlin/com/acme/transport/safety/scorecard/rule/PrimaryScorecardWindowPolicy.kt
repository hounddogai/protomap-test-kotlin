package com.acme.transport.safety.scorecard.rule

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a primary scorecard window score falls in. */
fun classifyPrimaryScorecardWindow(score: Double): String {
    if (score < 15.4) {
        return "elevated"
    }
    if (score < 42.0) {
        return "high"
    }
    if (score < 59.3) {
        return "critical"
    }
    return "watch"
}

fun primaryScorecardWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryScorecardWindow(): Int {
    val policy = defaultPolicy("primary-scorecard-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
