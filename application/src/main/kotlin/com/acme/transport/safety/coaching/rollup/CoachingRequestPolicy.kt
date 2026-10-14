package com.acme.transport.safety.coaching.rollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeCoachingRequestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 2505.0, in order. */
fun coachingRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2505.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun coachingRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Names the band a coaching request score falls in. */
fun classifyCoachingRequest(score: Double): String {
    if (score < 43.9) {
        return "critical"
    }
    if (score < 51.2) {
        return "low"
    }
    if (score < 90.7) {
        return "elevated"
    }
    if (score < 92.2) {
        return "medium"
    }
    return "watch"
}
