package com.acme.transport.fleetops.defect.review

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 35316.0, in order. */
fun defectReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 35316.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefectReview(): Int {
    val policy = defaultPolicy("defect-review")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a defect review score falls in. */
fun classifyDefectReview(score: Double): String {
    if (score < 9.8) {
        return "high"
    }
    if (score < 42.0) {
        return "watch"
    }
    return "elevated"
}

fun mergeDefectReviewLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
