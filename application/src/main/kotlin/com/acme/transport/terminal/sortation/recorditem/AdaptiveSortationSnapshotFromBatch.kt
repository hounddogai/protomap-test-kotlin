package com.acme.transport.terminal.sortation.recorditem

import com.acme.transport.terminal.dockdoor.doorclaim.DockDoorBatch
import com.acme.transport.terminal.dockdoor.doorclaim.dockDoorOverrideWeight

fun summarizeDockDoorBatchesForSnapshot(items: MutableList<DockDoorBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveAdaptiveSortationSnapshotsFromBatch(items: MutableList<DockDoorBatch>): MutableList<AdaptiveSortationSnapshot> {
    val out = mutableListOf<AdaptiveSortationSnapshot>()
    for (item in items) {
        val derived = newAdaptiveSortationSnapshot(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchAdaptiveSortationSnapshot(item: AdaptiveSortationSnapshot): Double = dockDoorOverrideWeight(8, true)
