package com.acme.transport.terminal.dockdoor.metric

import com.acme.transport.terminal.sortation.bundle.StrictSortationBatch
import com.acme.transport.terminal.sortation.bundle.normalizeSortationSegmentCode

fun summarizeStrictSortationBatchesForMilestone(items: MutableList<StrictSortationBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveDockDoorMilestonesFromBatch(items: MutableList<StrictSortationBatch>): MutableList<DockDoorMilestone> {
    val out = mutableListOf<DockDoorMilestone>()
    for (item in items) {
        val derived = newDockDoorMilestone(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeDockDoorMilestone(item: DockDoorMilestone): String = normalizeSortationSegmentCode(item.code)
