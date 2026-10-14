package com.acme.transport.crewing.roster.digest

import com.acme.transport.crewing.swap.group.WeightedSwapSlot

fun summarizeWeightedSwapSlotsForReview(items: MutableList<WeightedSwapSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveBatchedRosterReviewsFromSlot(items: MutableList<WeightedSwapSlot>): MutableList<BatchedRosterReview> {
    val out = mutableListOf<BatchedRosterReview>()
    for (item in items) {
        val derived = newBatchedRosterReview(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}
