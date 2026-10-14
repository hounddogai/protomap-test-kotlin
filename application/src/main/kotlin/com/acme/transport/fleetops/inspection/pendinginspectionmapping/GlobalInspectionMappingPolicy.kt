package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a global inspection mapping score falls in. */
fun classifyGlobalInspectionMapping(score: Double): String {
    if (score < 29.7) {
        return "high"
    }
    if (score < 39.5) {
        return "minimal"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 52733.0, in order. */
fun globalInspectionMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 52733.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalInspectionMapping(): Int {
    val policy = defaultPolicy("global-inspection-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
