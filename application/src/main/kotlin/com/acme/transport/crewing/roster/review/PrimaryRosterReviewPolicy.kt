package com.acme.transport.crewing.roster.review

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a primary roster review score falls in. */
fun classifyPrimaryRosterReview(score: Double): String {
    if (score < 2.9) {
        return "low"
    }
    if (score < 59.4) {
        return "ok"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 36563.0, in order. */
fun primaryRosterReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 36563.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun primaryRosterReviewBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun mergePrimaryRosterReviewLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
