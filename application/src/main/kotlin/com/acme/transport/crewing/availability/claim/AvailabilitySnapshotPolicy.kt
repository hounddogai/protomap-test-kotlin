package com.acme.transport.crewing.availability.claim

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAvailabilitySnapshot(): Int {
    val policy = defaultPolicy("availability-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a availability snapshot score falls in. */
fun classifyAvailabilitySnapshot(score: Double): String {
    if (score < 39.7) {
        return "high"
    }
    if (score < 63.9) {
        return "low"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 31677.0, in order. */
fun availabilitySnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 31677.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
