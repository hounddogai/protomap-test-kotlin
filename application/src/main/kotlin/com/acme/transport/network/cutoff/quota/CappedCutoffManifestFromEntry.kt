package com.acme.transport.network.cutoff.quota

import com.acme.transport.network.transittime.proposal.SeasonalTransitTimeEntry
import com.acme.transport.network.transittime.proposal.globalTransitTimeLedgerBucket

fun summarizeSeasonalTransitTimeEntriesForManifest(items: MutableList<SeasonalTransitTimeEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveCappedCutoffManifestsFromEntry(items: MutableList<SeasonalTransitTimeEntry>): MutableList<CappedCutoffManifest> {
    val out = mutableListOf<CappedCutoffManifest>()
    for (item in items) {
        val derived = newCappedCutoffManifest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleCappedCutoffManifest(item: CappedCutoffManifest): String {
    return globalTransitTimeLedgerBucket(item.projectedExposure())
}
