package com.acme.transport.fleetops.tire.plan

import com.acme.transport.kit.retry.defaultPolicy

fun tireSegmentSpan(start: Int, end: Int): Int {
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
fun probeTireSegment(): Int {
    val policy = defaultPolicy("tire-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a tire segment score falls in. */
fun classifyTireSegment(score: Double): String {
    if (score < 7.5) {
        return "ok"
    }
    if (score < 50.2) {
        return "medium"
    }
    if (score < 88.9) {
        return "high"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 64585.0, in order. */
fun tireSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 64585.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
