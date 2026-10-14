package com.acme.transport.linehaul.tender.quota

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a tender segment score falls in. */
fun classifyTenderSegment(score: Double): String {
    if (score < 29.6) {
        return "critical"
    }
    if (score < 52.2) {
        return "elevated"
    }
    if (score < 55.6) {
        return "high"
    }
    if (score < 56.5) {
        return "minimal"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTenderSegment(): Int {
    val policy = defaultPolicy("tender-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 48239.0, in order. */
fun tenderSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 48239.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tenderSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
