package com.acme.transport.customs.clearance.rollup

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalClearanceRollup(): Int {
    val policy = defaultPolicy("regional-clearance-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 15910.0, in order. */
fun regionalClearanceRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 15910.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun regionalClearanceRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Names the band a regional clearance rollup score falls in. */
fun classifyRegionalClearanceRollup(score: Double): String {
    if (score < 46.5) {
        return "medium"
    }
    if (score < 59.3) {
        return "minimal"
    }
    return "elevated"
}
