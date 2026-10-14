package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.safety.scorecard.milestone.WeightedScorecardReview
import com.acme.transport.safety.scorecard.milestone.classifyPrimaryScorecardTarget

fun summarizeWeightedScorecardReviewsForAllocation(items: MutableList<WeightedScorecardReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveScorecardAllocationsFromReview(items: MutableList<WeightedScorecardReview>): MutableList<ScorecardAllocation> {
    val out = mutableListOf<ScorecardAllocation>()
    for (item in items) {
        val derived = newScorecardAllocation(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateScorecardAllocation(item: ScorecardAllocation): String {
    return classifyPrimaryScorecardTarget(item.adjustedVolume())
}
