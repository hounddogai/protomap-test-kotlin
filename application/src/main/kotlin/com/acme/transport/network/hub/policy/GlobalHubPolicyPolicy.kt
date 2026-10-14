package com.acme.transport.network.hub.policy

import com.acme.transport.kit.numbers.roundTo

fun globalHubPolicyBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 45622.0, in order. */
fun globalHubPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 45622.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun globalHubPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
