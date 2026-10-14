package com.acme.transport.terminal.sortation.sortationsnapshot

import com.acme.transport.terminal.traileryard.recorditem.DefaultTrailerYardCheckpoint
import com.acme.transport.terminal.traileryard.recorditem.cappedTrailerYardManifestWeight

fun summarizeDefaultTrailerYardCheckpointsForSnapshot(items: MutableList<DefaultTrailerYardCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveLegacySortationSnapshotsFromCheckpoint(items: MutableList<DefaultTrailerYardCheckpoint>): MutableList<LegacySortationSnapshot> {
    val out = mutableListOf<LegacySortationSnapshot>()
    for (item in items) {
        val derived = newLegacySortationSnapshot(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeLegacySortationSnapshot(item: LegacySortationSnapshot): Double = cappedTrailerYardManifestWeight(9, true)
