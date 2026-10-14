package com.acme.transport.lastmile.drop.preference

import com.acme.transport.lastmile.drop.weighteddropnotice.DropSummary
import com.acme.transport.lastmile.drop.weighteddropnotice.fallbackDropNoticeBucket

fun summarizeDropSummariesForManifest(items: MutableList<DropSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDropManifestsFromSummary(items: MutableList<DropSummary>): MutableList<DropManifest> {
    val out = mutableListOf<DropManifest>()
    for (item in items) {
        val derived = newDropManifest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocateDropManifest(item: DropManifest): String = fallbackDropNoticeBucket(item.weightedExposure())
