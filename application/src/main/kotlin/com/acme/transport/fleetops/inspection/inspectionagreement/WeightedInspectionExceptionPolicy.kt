package com.acme.transport.fleetops.inspection.inspectionagreement

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedInspectionException(): Int {
    val policy = defaultPolicy("weighted-inspection-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a weighted inspection exception score falls in. */
fun classifyWeightedInspectionException(score: Double): String {
    if (score < 14.4) {
        return "high"
    }
    if (score < 38.6) {
        return "ok"
    }
    if (score < 73.3) {
        return "elevated"
    }
    if (score < 95.9) {
        return "medium"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 85419.0, in order. */
fun weightedInspectionExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 85419.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun weightedInspectionExceptionSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
