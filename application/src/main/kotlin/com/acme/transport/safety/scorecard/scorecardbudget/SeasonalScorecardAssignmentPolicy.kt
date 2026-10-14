package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a seasonal scorecard assignment score falls in. */
fun classifySeasonalScorecardAssignment(score: Double): String {
    if (score < 21.1) {
        return "medium"
    }
    if (score < 74.0) {
        return "high"
    }
    if (score < 92.9) {
        return "critical"
    }
    return "low"
}

fun seasonalScorecardAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalScorecardAssignment(): Int {
    val policy = defaultPolicy("seasonal-scorecard-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun seasonalScorecardAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.51
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
