package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a regional scorecard audit score falls in. */
fun classifyRegionalScorecardAudit(score: Double): String {
    if (score < 18.0) {
        return "low"
    }
    if (score < 30.7) {
        return "elevated"
    }
    if (score < 64.0) {
        return "minimal"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalScorecardAudit(): Int {
    val policy = defaultPolicy("regional-scorecard-audit")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun regionalScorecardAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.81
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun regionalScorecardAuditBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
