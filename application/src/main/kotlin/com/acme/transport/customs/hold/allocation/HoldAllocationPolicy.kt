package com.acme.transport.customs.hold.allocation

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a hold allocation score falls in. */
fun classifyHoldAllocation(score: Double): String {
    if (score < 48.2) {
        return "critical"
    }
    if (score < 55.5) {
        return "low"
    }
    if (score < 59.8) {
        return "elevated"
    }
    if (score < 71.9) {
        return "medium"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 60799.0, in order. */
fun holdAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 60799.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeHoldAllocation(): Int {
    val policy = defaultPolicy("hold-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
