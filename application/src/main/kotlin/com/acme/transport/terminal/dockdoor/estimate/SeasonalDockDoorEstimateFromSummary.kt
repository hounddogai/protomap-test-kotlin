package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.terminal.dwell.milestone.DwellSummary
import com.acme.transport.terminal.dwell.milestone.dwellSummaryBucket

fun summarizeDwellSummariesForEstimate(items: MutableList<DwellSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveSeasonalDockDoorEstimatesFromSummary(items: MutableList<DwellSummary>): MutableList<SeasonalDockDoorEstimate> {
    val out = mutableListOf<SeasonalDockDoorEstimate>()
    for (item in items) {
        val derived = newSeasonalDockDoorEstimate(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeSeasonalDockDoorEstimate(item: SeasonalDockDoorEstimate): String {
    return dwellSummaryBucket(item.measuredValue())
}
