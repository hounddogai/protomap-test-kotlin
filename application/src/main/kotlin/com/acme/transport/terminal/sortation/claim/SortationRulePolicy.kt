package com.acme.transport.terminal.sortation.claim

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 23491.0, in order. */
fun sortationRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 23491.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a sortation rule score falls in. */
fun classifySortationRule(score: Double): String {
    if (score < 61.8) {
        return "low"
    }
    if (score < 81.5) {
        return "ok"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSortationRule(): Int {
    val policy = defaultPolicy("sortation-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
