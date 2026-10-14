package com.acme.transport.terminal.traileryard.manifest

import com.acme.transport.terminal.dwell.cycle.DwellCycle
import com.acme.transport.terminal.dwell.cycle.dwellEntryBucket

fun summarizeDwellCyclesForCheckpoint(items: MutableList<DwellCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveTrailerYardCheckpointsFromCycle(items: MutableList<DwellCycle>): MutableList<TrailerYardCheckpoint> {
    val out = mutableListOf<TrailerYardCheckpoint>()
    for (item in items) {
        val derived = newTrailerYardCheckpoint(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateTrailerYardCheckpoint(item: TrailerYardCheckpoint): String = dwellEntryBucket(item.weightedVolume())
