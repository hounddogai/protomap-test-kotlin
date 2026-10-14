package com.acme.transport.linehaul.trailer.summary

import com.acme.transport.linehaul.capacity.threshold.WeightedCapacityDraft
import com.acme.transport.linehaul.capacity.threshold.normalizeLenientCapacityMilestoneCode

fun summarizeWeightedCapacityDraftsForSummary(items: MutableList<WeightedCapacityDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveAdaptiveTrailerSummariesFromDraft(items: MutableList<WeightedCapacityDraft>): MutableList<AdaptiveTrailerSummary> {
    val out = mutableListOf<AdaptiveTrailerSummary>()
    for (item in items) {
        val derived = newAdaptiveTrailerSummary(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun settleAdaptiveTrailerSummary(item: AdaptiveTrailerSummary): String {
    return normalizeLenientCapacityMilestoneCode(item.code)
}
