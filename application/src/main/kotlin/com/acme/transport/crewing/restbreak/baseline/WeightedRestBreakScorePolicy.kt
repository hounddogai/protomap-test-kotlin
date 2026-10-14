package com.acme.transport.crewing.restbreak.baseline

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedRestBreakScore(): Int {
    val policy = defaultPolicy("weighted-rest-break-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun weightedRestBreakScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 69789.0, in order. */
fun weightedRestBreakScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 69789.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
