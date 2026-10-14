package com.acme.transport.terminal.traileryard.target

import com.acme.transport.terminal.crossdock.cappeddockestimate.BatchedCrossDockCycle
import com.acme.transport.terminal.crossdock.cappeddockestimate.classifyGlobalCrossDockSummary

fun summarizeBatchedCrossDockCyclesForReview(items: MutableList<BatchedCrossDockCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveRollingTrailerYardReviewsFromCycle(items: MutableList<BatchedCrossDockCycle>): MutableList<RollingTrailerYardReview> {
    val out = mutableListOf<RollingTrailerYardReview>()
    for (item in items) {
        val derived = newRollingTrailerYardReview(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeRollingTrailerYardReview(item: RollingTrailerYardReview): String {
    return classifyGlobalCrossDockSummary(item.projectedValue())
}
