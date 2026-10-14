package com.acme.transport.crewing.swap.score

import com.acme.transport.crewing.availability.adjustment.PendingAvailabilityReview
import com.acme.transport.crewing.availability.adjustment.availabilityRollupBucket

fun summarizePendingAvailabilityReviewsForRule(items: MutableList<PendingAvailabilityReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveSwapRulesFromReview(items: MutableList<PendingAvailabilityReview>): MutableList<SwapRule> {
    val out = mutableListOf<SwapRule>()
    for (item in items) {
        val derived = newSwapRule(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateSwapRule(item: SwapRule): String = availabilityRollupBucket(item.projectedLoad())
