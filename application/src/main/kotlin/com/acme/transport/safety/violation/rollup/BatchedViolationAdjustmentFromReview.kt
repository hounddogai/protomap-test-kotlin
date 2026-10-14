package com.acme.transport.safety.violation.rollup

import com.acme.transport.safety.nearmiss.baseline.StrictNearMissReview
import com.acme.transport.safety.nearmiss.baseline.nearMissBaselineBucket

fun summarizeStrictNearMissReviewsForAdjustment(items: MutableList<StrictNearMissReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveBatchedViolationAdjustmentsFromReview(items: MutableList<StrictNearMissReview>): MutableList<BatchedViolationAdjustment> {
    val out = mutableListOf<BatchedViolationAdjustment>()
    for (item in items) {
        val derived = newBatchedViolationAdjustment(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyBatchedViolationAdjustment(item: BatchedViolationAdjustment): String {
    return nearMissBaselineBucket(item.projectedTotal())
}
