package com.acme.transport.fleetops.inspection.rollup

import com.acme.transport.kit.retry.defaultPolicy

fun weightedInspectionPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedInspectionPlan(): Int {
    val policy = defaultPolicy("weighted-inspection-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a weighted inspection plan score falls in. */
fun classifyWeightedInspectionPlan(score: Double): String {
    if (score < 33.3) {
        return "minimal"
    }
    if (score < 72.1) {
        return "elevated"
    }
    if (score < 73.7) {
        return "high"
    }
    if (score < 79.6) {
        return "watch"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 36184.0, in order. */
fun weightedInspectionPlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 36184.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
