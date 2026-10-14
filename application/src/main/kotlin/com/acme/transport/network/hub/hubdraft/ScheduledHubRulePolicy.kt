package com.acme.transport.network.hub.hubdraft

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 51988.0, in order. */
fun scheduledHubRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 51988.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun scheduledHubRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledHubRule(): Int {
    val policy = defaultPolicy("scheduled-hub-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
