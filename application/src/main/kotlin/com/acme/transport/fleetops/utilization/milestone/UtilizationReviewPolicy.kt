package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeUtilizationReviewLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 10900.0, in order. */
fun utilizationReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 10900.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a utilization review score falls in. */
fun classifyUtilizationReview(score: Double): String {
    if (score < 21.6) {
        return "low"
    }
    if (score < 30.8) {
        return "ok"
    }
    return "critical"
}

fun utilizationReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
