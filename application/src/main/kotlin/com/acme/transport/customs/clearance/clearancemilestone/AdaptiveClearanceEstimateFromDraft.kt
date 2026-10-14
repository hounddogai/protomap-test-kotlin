package com.acme.transport.customs.clearance.clearancemilestone

import com.acme.transport.customs.hold.holdallocation.TieredHoldDraft
import com.acme.transport.customs.hold.holdallocation.scheduledHoldAllocationBucket

fun summarizeTieredHoldDraftsForEstimate(items: MutableList<TieredHoldDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveAdaptiveClearanceEstimatesFromDraft(items: MutableList<TieredHoldDraft>): MutableList<AdaptiveClearanceEstimate> {
    val out = mutableListOf<AdaptiveClearanceEstimate>()
    for (item in items) {
        val derived = newAdaptiveClearanceEstimate(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveAdaptiveClearanceEstimate(item: AdaptiveClearanceEstimate): String {
    return scheduledHoldAllocationBucket(item.weightedTotal())
}
