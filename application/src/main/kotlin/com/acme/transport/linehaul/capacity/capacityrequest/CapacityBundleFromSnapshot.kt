package com.acme.transport.linehaul.capacity.capacityrequest

import com.acme.transport.linehaul.capacity.snapshot.SeasonalCapacitySnapshot
import com.acme.transport.linehaul.capacity.snapshot.seasonalCapacitySnapshotBucket

fun summarizeSeasonalCapacitySnapshotsForBundle(items: MutableList<SeasonalCapacitySnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveCapacityBundlesFromSnapshot(items: MutableList<SeasonalCapacitySnapshot>): MutableList<CapacityBundle> {
    val out = mutableListOf<CapacityBundle>()
    for (item in items) {
        val derived = newCapacityBundle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun settleCapacityBundle(item: CapacityBundle): String = seasonalCapacitySnapshotBucket(item.weightedVolume())
