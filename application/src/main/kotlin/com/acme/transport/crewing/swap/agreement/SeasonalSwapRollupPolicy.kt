package com.acme.transport.crewing.swap.agreement

import com.acme.transport.kit.retry.defaultPolicy

fun seasonalSwapRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalSwapRollup(): Int {
    val policy = defaultPolicy("seasonal-swap-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 46064.0, in order. */
fun seasonalSwapRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46064.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
