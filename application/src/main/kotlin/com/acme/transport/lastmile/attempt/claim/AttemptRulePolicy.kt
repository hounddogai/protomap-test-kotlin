package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAttemptRule(): Int {
    val policy = defaultPolicy("attempt-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun attemptRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 49329.0, in order. */
fun attemptRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 49329.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a attempt rule score falls in. */
fun classifyAttemptRule(score: Double): String {
    if (score < 34.6) {
        return "low"
    }
    if (score < 47.3) {
        return "ok"
    }
    return "critical"
}
