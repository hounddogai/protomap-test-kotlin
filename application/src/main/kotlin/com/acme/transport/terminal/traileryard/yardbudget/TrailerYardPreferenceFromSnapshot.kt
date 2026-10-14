package com.acme.transport.terminal.traileryard.yardbudget

import com.acme.transport.terminal.sortation.sortationreview.PrimarySortationSnapshot
import com.acme.transport.terminal.sortation.sortationreview.legacySortationReviewBucket

fun summarizePrimarySortationSnapshotsForPreference(items: MutableList<PrimarySortationSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveTrailerYardPreferencesFromSnapshot(items: MutableList<PrimarySortationSnapshot>): MutableList<TrailerYardPreference> {
    val out = mutableListOf<TrailerYardPreference>()
    for (item in items) {
        val derived = newTrailerYardPreference(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateTrailerYardPreference(item: TrailerYardPreference): String {
    return legacySortationReviewBucket(item.effectiveExposure())
}
