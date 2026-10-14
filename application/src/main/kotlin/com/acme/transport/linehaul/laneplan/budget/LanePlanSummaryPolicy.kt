package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a lane plan summary score falls in. */
fun classifyLanePlanSummary(score: Double): String {
    if (score < 23.0) {
        return "watch"
    }
    if (score < 31.4) {
        return "critical"
    }
    if (score < 50.8) {
        return "elevated"
    }
    if (score < 73.8) {
        return "medium"
    }
    return "ok"
}

fun lanePlanSummaryBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLanePlanSummary(): Int {
    val policy = defaultPolicy("lane-plan-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
