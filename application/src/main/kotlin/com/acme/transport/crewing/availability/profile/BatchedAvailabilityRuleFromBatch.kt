package com.acme.transport.crewing.availability.profile

import com.acme.transport.crewing.swap.slot.SwapBatch
import com.acme.transport.crewing.swap.slot.swapBatchBucket

fun summarizeSwapBatchesForRule(items: MutableList<SwapBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveBatchedAvailabilityRulesFromBatch(items: MutableList<SwapBatch>): MutableList<BatchedAvailabilityRule> {
    val out = mutableListOf<BatchedAvailabilityRule>()
    for (item in items) {
        val derived = newBatchedAvailabilityRule(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchBatchedAvailabilityRule(item: BatchedAvailabilityRule): String = swapBatchBucket(item.netLoad())
