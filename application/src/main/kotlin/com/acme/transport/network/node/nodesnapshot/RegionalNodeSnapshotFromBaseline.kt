package com.acme.transport.network.node.nodesnapshot

import com.acme.transport.network.hub.plan.TieredHubBaseline
import com.acme.transport.network.hub.plan.normalizeTieredHubBaselineCode

fun summarizeTieredHubBaselinesForSnapshot(items: MutableList<TieredHubBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveRegionalNodeSnapshotsFromBaseline(items: MutableList<TieredHubBaseline>): MutableList<RegionalNodeSnapshot> {
    val out = mutableListOf<RegionalNodeSnapshot>()
    for (item in items) {
        val derived = newRegionalNodeSnapshot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateRegionalNodeSnapshot(item: RegionalNodeSnapshot): String = normalizeTieredHubBaselineCode(item.code)
