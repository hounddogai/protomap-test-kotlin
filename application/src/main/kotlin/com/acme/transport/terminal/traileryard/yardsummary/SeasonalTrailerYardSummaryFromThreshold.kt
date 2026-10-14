package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.terminal.sortation.snapshot.SortationThreshold
import com.acme.transport.terminal.sortation.snapshot.sortationOverrideWeight

fun summarizeSortationThresholdsForSummary(items: MutableList<SortationThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveSeasonalTrailerYardSummariesFromThreshold(items: MutableList<SortationThreshold>): MutableList<SeasonalTrailerYardSummary> {
    val out = mutableListOf<SeasonalTrailerYardSummary>()
    for (item in items) {
        val derived = newSeasonalTrailerYardSummary(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileSeasonalTrailerYardSummary(item: SeasonalTrailerYardSummary): Double {
    return sortationOverrideWeight(6, true)
}
