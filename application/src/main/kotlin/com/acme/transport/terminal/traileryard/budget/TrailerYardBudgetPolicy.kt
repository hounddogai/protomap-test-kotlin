package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTrailerYardBudget(): Int {
    val policy = defaultPolicy("trailer-yard-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 89832.0, in order. */
fun trailerYardBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 89832.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun trailerYardBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Names the band a trailer yard budget score falls in. */
fun classifyTrailerYardBudget(score: Double): String {
    if (score < 38.9) {
        return "minimal"
    }
    if (score < 39.6) {
        return "low"
    }
    if (score < 46.3) {
        return "ok"
    }
    return "high"
}
