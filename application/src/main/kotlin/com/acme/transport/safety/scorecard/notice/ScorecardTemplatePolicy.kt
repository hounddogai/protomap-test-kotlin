package com.acme.transport.safety.scorecard.notice

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun scorecardTemplateBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a scorecard template score falls in. */
fun classifyScorecardTemplate(score: Double): String {
    if (score < 47.9) {
        return "medium"
    }
    if (score < 79.7) {
        return "low"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScorecardTemplate(): Int {
    val policy = defaultPolicy("scorecard-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
