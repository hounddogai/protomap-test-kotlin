package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun rollingRosterRollupBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a rolling roster rollup score falls in. */
fun classifyRollingRosterRollup(score: Double): String {
    if (score < 43.3) {
        return "critical"
    }
    if (score < 67.7) {
        return "medium"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 12667.0, in order. */
fun rollingRosterRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 12667.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingRosterRollup(): Int {
    val policy = defaultPolicy("rolling-roster-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
