package com.acme.transport.terminal.sortation.request

import com.acme.transport.terminal.traileryard.rollup.TrailerYardSnapshot
import com.acme.transport.terminal.traileryard.rollup.trailerYardSnapshotBucket

fun summarizeTrailerYardSnapshotsForManifest(items: MutableList<TrailerYardSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveStrictSortationManifestsFromSnapshot(items: MutableList<TrailerYardSnapshot>): MutableList<StrictSortationManifest> {
    val out = mutableListOf<StrictSortationManifest>()
    for (item in items) {
        val derived = newStrictSortationManifest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchStrictSortationManifest(item: StrictSortationManifest): String {
    return trailerYardSnapshotBucket(item.projectedTotal())
}
