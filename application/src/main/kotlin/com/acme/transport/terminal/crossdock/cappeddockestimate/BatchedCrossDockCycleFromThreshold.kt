package com.acme.transport.terminal.crossdock.cappeddockestimate

import com.acme.transport.terminal.crossdock.dockclaim.BatchedCrossDockThreshold
import com.acme.transport.terminal.crossdock.dockclaim.classifyScheduledCrossDockClaim

fun summarizeBatchedCrossDockThresholdsForCycle(items: MutableList<BatchedCrossDockThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveBatchedCrossDockCyclesFromThreshold(items: MutableList<BatchedCrossDockThreshold>): MutableList<BatchedCrossDockCycle> {
    val out = mutableListOf<BatchedCrossDockCycle>()
    for (item in items) {
        val derived = newBatchedCrossDockCycle(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeBatchedCrossDockCycle(item: BatchedCrossDockCycle): String {
    return classifyScheduledCrossDockClaim(item.effectiveLoad())
}
