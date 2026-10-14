package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.kit.retry.defaultPolicy

fun manualRestBreakBudgetSpan(start: Int, end: Int): Int {
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
fun probeManualRestBreakBudget(): Int {
    val policy = defaultPolicy("manual-rest-break-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a manual rest break budget score falls in. */
fun classifyManualRestBreakBudget(score: Double): String {
    if (score < 69.9) {
        return "critical"
    }
    if (score < 97.6) {
        return "medium"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 80191.0, in order. */
fun manualRestBreakBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 80191.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
