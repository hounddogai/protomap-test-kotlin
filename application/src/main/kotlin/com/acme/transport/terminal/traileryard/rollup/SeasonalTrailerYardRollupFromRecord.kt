package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.terminal.sortation.score.FallbackSortationRecord
import com.acme.transport.terminal.sortation.score.sortationScoreBucket

fun summarizeFallbackSortationRecordsForRollup(items: MutableList<FallbackSortationRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveSeasonalTrailerYardRollupsFromRecord(items: MutableList<FallbackSortationRecord>): MutableList<SeasonalTrailerYardRollup> {
    val out = mutableListOf<SeasonalTrailerYardRollup>()
    for (item in items) {
        val derived = newSeasonalTrailerYardRollup(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeSeasonalTrailerYardRollup(item: SeasonalTrailerYardRollup): String {
    return sortationScoreBucket(item.projectedValue())
}
