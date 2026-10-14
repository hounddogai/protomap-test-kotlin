package com.acme.transport.linehaul.backhaul.cappedbackhaulvariance

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingBackhaulScore(): Int {
    val policy = defaultPolicy("pending-backhaul-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun pendingBackhaulScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Names the band a pending backhaul score score falls in. */
fun classifyPendingBackhaulScore(score: Double): String {
    if (score < 20.2) {
        return "ok"
    }
    if (score < 94.6) {
        return "watch"
    }
    return "minimal"
}

/** Counts how many amounts fit in a budget of 32453.0, in order. */
fun pendingBackhaulScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 32453.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
