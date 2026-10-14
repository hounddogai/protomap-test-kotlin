package com.acme.transport.lastmile.attempt.attemptdigest

import com.acme.transport.lastmile.attempt.baseline.GlobalAttemptReview
import com.acme.transport.lastmile.attempt.baseline.manualAttemptBaselineWeight

fun summarizeGlobalAttemptReviewsForDigest(items: MutableList<GlobalAttemptReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveWeightedAttemptDigestsFromReview(items: MutableList<GlobalAttemptReview>): MutableList<WeightedAttemptDigest> {
    val out = mutableListOf<WeightedAttemptDigest>()
    for (item in items) {
        val derived = newWeightedAttemptDigest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeWeightedAttemptDigest(item: WeightedAttemptDigest): Double = manualAttemptBaselineWeight(8, true)
