package com.acme.transport.network.hub.proposal

import com.acme.transport.network.cutoff.target.CutoffRule
import com.acme.transport.network.cutoff.target.cutoffSnapshotBucket

fun summarizeCutoffRulesForEntry(items: MutableList<CutoffRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveDefaultHubEntriesFromRule(items: MutableList<CutoffRule>): MutableList<DefaultHubEntry> {
    val out = mutableListOf<DefaultHubEntry>()
    for (item in items) {
        val derived = newDefaultHubEntry(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun matchDefaultHubEntry(item: DefaultHubEntry): String = cutoffSnapshotBucket(item.projectedValue())
