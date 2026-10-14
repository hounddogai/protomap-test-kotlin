package com.acme.transport.safety.scorecard.slot

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun scorecardAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.42
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun scorecardAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Names the band a scorecard assignment score falls in. */
fun classifyScorecardAssignment(score: Double): String {
    if (score < 51.6) {
        return "elevated"
    }
    if (score < 96.5) {
        return "critical"
    }
    if (score < 98.7) {
        return "low"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScorecardAssignment(): Int {
    val policy = defaultPolicy("scorecard-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
