package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.terminal.sortation.window.PendingSortationBatch
import com.acme.transport.terminal.sortation.window.defaultSortationReviewBucket

fun summarizePendingSortationBatchesForPolicy(items: MutableList<PendingSortationBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveRollingTrailerYardPoliciesFromBatch(items: MutableList<PendingSortationBatch>): MutableList<RollingTrailerYardPolicy> {
    val out = mutableListOf<RollingTrailerYardPolicy>()
    for (item in items) {
        val derived = newRollingTrailerYardPolicy(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankRollingTrailerYardPolicy(item: RollingTrailerYardPolicy): String {
    return defaultSortationReviewBucket(item.weightedTotal())
}
