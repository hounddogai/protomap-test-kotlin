package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.retry.defaultPolicy

fun primaryTrailerYardDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Names the band a primary trailer yard draft score falls in. */
fun classifyPrimaryTrailerYardDraft(score: Double): String {
    if (score < 4.7) {
        return "minimal"
    }
    if (score < 25.4) {
        return "ok"
    }
    if (score < 57.9) {
        return "high"
    }
    if (score < 96.2) {
        return "medium"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryTrailerYardDraft(): Int {
    val policy = defaultPolicy("primary-trailer-yard-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 90078.0, in order. */
fun primaryTrailerYardDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 90078.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
