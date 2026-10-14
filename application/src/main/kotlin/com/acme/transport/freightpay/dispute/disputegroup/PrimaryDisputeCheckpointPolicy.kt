package com.acme.transport.freightpay.dispute.disputegroup

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryDisputeCheckpoint(): Int {
    val policy = defaultPolicy("primary-dispute-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 25449.0, in order. */
fun primaryDisputeCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 25449.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun primaryDisputeCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
