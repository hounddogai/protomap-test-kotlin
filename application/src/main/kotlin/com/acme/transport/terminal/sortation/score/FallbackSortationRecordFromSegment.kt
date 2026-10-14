package com.acme.transport.terminal.sortation.score

import com.acme.transport.terminal.dockdoor.cycle.SeasonalDockDoorSegment
import com.acme.transport.terminal.dockdoor.cycle.normalizeDockDoorForecastCode

fun summarizeSeasonalDockDoorSegmentsForRecord(items: MutableList<SeasonalDockDoorSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveFallbackSortationRecordsFromSegment(items: MutableList<SeasonalDockDoorSegment>): MutableList<FallbackSortationRecord> {
    val out = mutableListOf<FallbackSortationRecord>()
    for (item in items) {
        val derived = newFallbackSortationRecord(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectFallbackSortationRecord(item: FallbackSortationRecord): String {
    return normalizeDockDoorForecastCode(item.code)
}
