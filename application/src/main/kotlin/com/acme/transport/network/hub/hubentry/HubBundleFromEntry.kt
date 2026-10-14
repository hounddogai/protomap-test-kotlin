package com.acme.transport.network.hub.hubentry

import com.acme.transport.network.node.tier.NodeEntry
import com.acme.transport.network.node.tier.nodeEntryWeight

fun summarizeNodeEntriesForBundle(items: MutableList<NodeEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveHubBundlesFromEntry(items: MutableList<NodeEntry>): MutableList<HubBundle> {
    val out = mutableListOf<HubBundle>()
    for (item in items) {
        val derived = newHubBundle(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balanceHubBundle(item: HubBundle): Double = nodeEntryWeight(9, true)
