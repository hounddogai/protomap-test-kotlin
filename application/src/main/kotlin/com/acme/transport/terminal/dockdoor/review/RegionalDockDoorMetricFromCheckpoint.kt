package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.terminal.sortation.plan.SortationCheckpoint
import com.acme.transport.terminal.sortation.plan.sortationCheckpointBucket

fun summarizeSortationCheckpointsForMetric(items: MutableList<SortationCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveRegionalDockDoorMetricsFromCheckpoint(items: MutableList<SortationCheckpoint>): MutableList<RegionalDockDoorMetric> {
    val out = mutableListOf<RegionalDockDoorMetric>()
    for (item in items) {
        val derived = newRegionalDockDoorMetric(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planRegionalDockDoorMetric(item: RegionalDockDoorMetric): String {
    return sortationCheckpointBucket(item.weightedValue())
}
