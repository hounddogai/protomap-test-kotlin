package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.terminal.sortation.baseline.LegacySortationRecord
import com.acme.transport.terminal.sortation.baseline.legacySortationRecordWeight

fun summarizeLegacySortationRecordsForBaseline(items: MutableList<LegacySortationRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveTieredCrossDockBaselinesFromRecord(items: MutableList<LegacySortationRecord>): MutableList<TieredCrossDockBaseline> {
    val out = mutableListOf<TieredCrossDockBaseline>()
    for (item in items) {
        val derived = newTieredCrossDockBaseline(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectTieredCrossDockBaseline(item: TieredCrossDockBaseline): Double = legacySortationRecordWeight(2, true)
