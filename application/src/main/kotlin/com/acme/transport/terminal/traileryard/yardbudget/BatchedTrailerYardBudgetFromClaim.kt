package com.acme.transport.terminal.traileryard.yardbudget

import com.acme.transport.terminal.crossdock.rollup.TieredCrossDockClaim
import com.acme.transport.terminal.crossdock.rollup.classifyPendingCrossDockSummary

fun summarizeTieredCrossDockClaimsForBudget(items: MutableList<TieredCrossDockClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveBatchedTrailerYardBudgetsFromClaim(items: MutableList<TieredCrossDockClaim>): MutableList<BatchedTrailerYardBudget> {
    val out = mutableListOf<BatchedTrailerYardBudget>()
    for (item in items) {
        val derived = newBatchedTrailerYardBudget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeBatchedTrailerYardBudget(item: BatchedTrailerYardBudget): String {
    return classifyPendingCrossDockSummary(item.projectedVolume())
}
