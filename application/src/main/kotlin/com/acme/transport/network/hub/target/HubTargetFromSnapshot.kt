package com.acme.transport.network.hub.target

import com.acme.transport.network.node.plan.NodeSnapshot
import com.acme.transport.network.node.plan.nodeSnapshotBucket

fun summarizeNodeSnapshotsForTarget(items: MutableList<NodeSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveHubTargetsFromSnapshot(items: MutableList<NodeSnapshot>): MutableList<HubTarget> {
    val out = mutableListOf<HubTarget>()
    for (item in items) {
        val derived = newHubTarget(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectHubTarget(item: HubTarget): String = nodeSnapshotBucket(item.weightedVolume())
