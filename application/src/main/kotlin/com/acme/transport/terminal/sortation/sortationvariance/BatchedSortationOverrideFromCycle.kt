package com.acme.transport.terminal.sortation.sortationvariance

import com.acme.transport.terminal.crossdock.cappeddockestimate.BatchedCrossDockCycle
import com.acme.transport.terminal.crossdock.cappeddockestimate.classifyGlobalCrossDockSummary

fun summarizeBatchedCrossDockCyclesForOverride(items: MutableList<BatchedCrossDockCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveBatchedSortationOverridesFromCycle(items: MutableList<BatchedCrossDockCycle>): MutableList<BatchedSortationOverride> {
    val out = mutableListOf<BatchedSortationOverride>()
    for (item in items) {
        val derived = newBatchedSortationOverride(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeBatchedSortationOverride(item: BatchedSortationOverride): String {
    return classifyGlobalCrossDockSummary(item.projectedVolume())
}
