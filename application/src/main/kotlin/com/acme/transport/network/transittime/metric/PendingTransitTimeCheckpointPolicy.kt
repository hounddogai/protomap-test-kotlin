package com.acme.transport.network.transittime.metric

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 27284.0, in order. */
fun pendingTransitTimeCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 27284.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingTransitTimeCheckpoint(): Int {
    val policy = defaultPolicy("pending-transit-time-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a pending transit time checkpoint score falls in. */
fun classifyPendingTransitTimeCheckpoint(score: Double): String {
    if (score < 41.0) {
        return "watch"
    }
    if (score < 78.9) {
        return "critical"
    }
    return "minimal"
}
