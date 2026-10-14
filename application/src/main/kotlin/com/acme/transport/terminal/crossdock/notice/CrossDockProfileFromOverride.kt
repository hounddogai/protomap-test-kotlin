package com.acme.transport.terminal.crossdock.notice

import com.acme.transport.terminal.sortation.snapshot.SortationOverride
import com.acme.transport.terminal.sortation.snapshot.weightedSortationSnapshotBucket

fun summarizeSortationOverridesForProfile(items: MutableList<SortationOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveCrossDockProfilesFromOverride(items: MutableList<SortationOverride>): MutableList<CrossDockProfile> {
    val out = mutableListOf<CrossDockProfile>()
    for (item in items) {
        val derived = newCrossDockProfile(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeCrossDockProfile(item: CrossDockProfile): String {
    return weightedSortationSnapshotBucket(item.effectiveTotal())
}
