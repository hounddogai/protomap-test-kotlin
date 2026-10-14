package com.acme.transport.fleetops.leasereturn.entry

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a primary lease return budget score falls in. */
fun classifyPrimaryLeaseReturnBudget(score: Double): String {
    if (score < 6.2) {
        return "minimal"
    }
    if (score < 8.7) {
        return "medium"
    }
    if (score < 28.6) {
        return "ok"
    }
    if (score < 46.8) {
        return "critical"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 63930.0, in order. */
fun primaryLeaseReturnBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 63930.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryLeaseReturnBudget(): Int {
    val policy = defaultPolicy("primary-lease-return-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
