package com.acme.transport.terminal.crossdock.reservation

import com.acme.transport.terminal.crossdock.rollup.CrossDockBudget
import com.acme.transport.terminal.crossdock.rollup.tieredCrossDockRollupWeight

fun summarizeCrossDockBudgetsForSummary(items: MutableList<CrossDockBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveAdaptiveCrossDockSummariesFromBudget(items: MutableList<CrossDockBudget>): MutableList<AdaptiveCrossDockSummary> {
    val out = mutableListOf<AdaptiveCrossDockSummary>()
    for (item in items) {
        val derived = newAdaptiveCrossDockSummary(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeAdaptiveCrossDockSummary(item: AdaptiveCrossDockSummary): Double {
    return tieredCrossDockRollupWeight(6, true)
}
