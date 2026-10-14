package com.acme.transport.freightpay.accessorial.template

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 9146.0, in order. */
fun fallbackAccessorialMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 9146.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fallbackAccessorialMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackAccessorialMilestone(): Int {
    val policy = defaultPolicy("fallback-accessorial-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a fallback accessorial milestone score falls in. */
fun classifyFallbackAccessorialMilestone(score: Double): String {
    if (score < 14.2) {
        return "critical"
    }
    if (score < 49.2) {
        return "high"
    }
    return "ok"
}
