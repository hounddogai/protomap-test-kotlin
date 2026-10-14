package com.acme.transport.network.node.nodepolicy

import com.acme.transport.network.hub.entry.CappedHubEntry
import com.acme.transport.network.hub.entry.hubManifestWeight

fun summarizeCappedHubEntriesForBundle(items: MutableList<CappedHubEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveNodeBundlesFromEntry(items: MutableList<CappedHubEntry>): MutableList<NodeBundle> {
    val out = mutableListOf<NodeBundle>()
    for (item in items) {
        val derived = newNodeBundle(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeNodeBundle(item: NodeBundle): Double = hubManifestWeight(3, true)
