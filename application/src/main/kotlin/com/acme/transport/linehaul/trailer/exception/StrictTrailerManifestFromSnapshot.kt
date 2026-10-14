package com.acme.transport.linehaul.trailer.exception

import com.acme.transport.linehaul.capacity.snapshot.SeasonalCapacitySnapshot
import com.acme.transport.linehaul.capacity.snapshot.classifyCapacityThreshold

fun summarizeSeasonalCapacitySnapshotsForManifest(items: MutableList<SeasonalCapacitySnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveStrictTrailerManifestsFromSnapshot(items: MutableList<SeasonalCapacitySnapshot>): MutableList<StrictTrailerManifest> {
    val out = mutableListOf<StrictTrailerManifest>()
    for (item in items) {
        val derived = newStrictTrailerManifest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocateStrictTrailerManifest(item: StrictTrailerManifest): String {
    return classifyCapacityThreshold(item.weightedTotal())
}
