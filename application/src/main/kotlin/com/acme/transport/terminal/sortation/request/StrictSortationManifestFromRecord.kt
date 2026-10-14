package com.acme.transport.terminal.sortation.request

import com.acme.transport.terminal.sortation.score.FallbackSortationRecord
import com.acme.transport.terminal.sortation.score.sortationScoreBucket

fun summarizeFallbackSortationRecordsForManifest(items: MutableList<FallbackSortationRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveStrictSortationManifestsFromRecord(items: MutableList<FallbackSortationRecord>): MutableList<StrictSortationManifest> {
    val out = mutableListOf<StrictSortationManifest>()
    for (item in items) {
        val derived = newStrictSortationManifest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyStrictSortationManifest(item: StrictSortationManifest): String {
    return sortationScoreBucket(item.projectedTotal())
}
