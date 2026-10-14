package com.acme.transport.network.cutoff.batch

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 74731.0, in order. */
fun primaryCutoffScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74731.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun primaryCutoffScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryCutoffScore(): Int {
    val policy = defaultPolicy("primary-cutoff-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
