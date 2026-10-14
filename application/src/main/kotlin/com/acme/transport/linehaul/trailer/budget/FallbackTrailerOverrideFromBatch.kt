package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.linehaul.capacity.summary.BatchedCapacityBatch
import com.acme.transport.linehaul.capacity.summary.normalizePendingCapacityRollupCode

fun summarizeBatchedCapacityBatchesForOverride(items: MutableList<BatchedCapacityBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveFallbackTrailerOverridesFromBatch(items: MutableList<BatchedCapacityBatch>): MutableList<FallbackTrailerOverride> {
    val out = mutableListOf<FallbackTrailerOverride>()
    for (item in items) {
        val derived = newFallbackTrailerOverride(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceFallbackTrailerOverride(item: FallbackTrailerOverride): String {
    return normalizePendingCapacityRollupCode(item.code)
}
