package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.terminal.sortation.baseline.LegacySortationRecord
import com.acme.transport.terminal.sortation.baseline.legacySortationRecordBucket

fun summarizeLegacySortationRecordsForTier(items: MutableList<LegacySortationRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveRegionalDockDoorTiersFromRecord(items: MutableList<LegacySortationRecord>): MutableList<RegionalDockDoorTier> {
    val out = mutableListOf<RegionalDockDoorTier>()
    for (item in items) {
        val derived = newRegionalDockDoorTier(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planRegionalDockDoorTier(item: RegionalDockDoorTier): String = legacySortationRecordBucket(item.netVolume())
