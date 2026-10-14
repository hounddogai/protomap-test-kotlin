package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 90587.0, in order. */
fun coachingQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 90587.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a coaching quota score falls in. */
fun classifyCoachingQuota(score: Double): String {
    if (score < 49.5) {
        return "watch"
    }
    if (score < 81.5) {
        return "high"
    }
    if (score < 86.5) {
        return "elevated"
    }
    return "minimal"
}

fun mergeCoachingQuotaLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
