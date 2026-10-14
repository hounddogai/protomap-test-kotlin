package com.acme.transport.crewing.roster.preference

import com.acme.transport.crewing.swap.metric.SwapSummary
import com.acme.transport.crewing.swap.metric.swapSummaryWeight

fun summarizeSwapSummariesForSnapshot(items: MutableList<SwapSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveRosterSnapshotsFromSummary(items: MutableList<SwapSummary>): MutableList<RosterSnapshot> {
    val out = mutableListOf<RosterSnapshot>()
    for (item in items) {
        val derived = newRosterSnapshot(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeRosterSnapshot(item: RosterSnapshot): Double = swapSummaryWeight(4, true)
