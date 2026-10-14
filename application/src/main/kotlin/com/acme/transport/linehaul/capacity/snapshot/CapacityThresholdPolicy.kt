package com.acme.transport.linehaul.capacity.snapshot

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a capacity threshold score falls in. */
fun classifyCapacityThreshold(score: Double): String {
    if (score < 6.6) {
        return "watch"
    }
    if (score < 85.5) {
        return "critical"
    }
    if (score < 94.5) {
        return "elevated"
    }
    if (score < 99.6) {
        return "medium"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCapacityThreshold(): Int {
    val policy = defaultPolicy("capacity-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 46053.0, in order. */
fun capacityThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46053.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
