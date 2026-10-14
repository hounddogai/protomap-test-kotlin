package com.acme.transport.crewing.dutyperiod.periodreview

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 54051.0, in order. */
fun manualDutyPeriodReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 54051.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a manual duty period review score falls in. */
fun classifyManualDutyPeriodReview(score: Double): String {
    if (score < 59.0) {
        return "minimal"
    }
    if (score < 62.3) {
        return "low"
    }
    if (score < 82.7) {
        return "high"
    }
    return "ok"
}

fun mergeManualDutyPeriodReviewLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
