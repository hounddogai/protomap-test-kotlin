package com.acme.transport.safety.scorecard.slot

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun tieredScorecardNoticeBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredScorecardNotice(): Int {
    val policy = defaultPolicy("tiered-scorecard-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a tiered scorecard notice score falls in. */
fun classifyTieredScorecardNotice(score: Double): String {
    if (score < 38.7) {
        return "watch"
    }
    if (score < 56.7) {
        return "medium"
    }
    return "low"
}
