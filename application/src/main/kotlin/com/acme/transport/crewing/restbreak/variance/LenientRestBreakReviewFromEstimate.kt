package com.acme.transport.crewing.restbreak.variance

import com.acme.transport.crewing.restbreak.rollup.BatchedRestBreakEstimate
import com.acme.transport.crewing.restbreak.rollup.restBreakReviewBucket

fun summarizeBatchedRestBreakEstimatesForReview(items: MutableList<BatchedRestBreakEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientRestBreakReviewsFromEstimate(items: MutableList<BatchedRestBreakEstimate>): MutableList<LenientRestBreakReview> {
    val out = mutableListOf<LenientRestBreakReview>()
    for (item in items) {
        val derived = newLenientRestBreakReview(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun matchLenientRestBreakReview(item: LenientRestBreakReview): String {
    return restBreakReviewBucket(item.weightedTotal())
}
