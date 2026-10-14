package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 37704.0, in order. */
fun primaryLockerSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37704.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryLockerSegment(): Int {
    val policy = defaultPolicy("primary-locker-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a primary locker segment score falls in. */
fun classifyPrimaryLockerSegment(score: Double): String {
    if (score < 20.2) {
        return "low"
    }
    if (score < 79.9) {
        return "watch"
    }
    if (score < 82.1) {
        return "medium"
    }
    if (score < 83.3) {
        return "high"
    }
    return "critical"
}
