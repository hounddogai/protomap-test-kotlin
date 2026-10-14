package com.acme.transport.network.node.score

import com.acme.transport.network.transittime.manifest.DefaultTransitTimeManifest
import com.acme.transport.network.transittime.manifest.transitTimeBaselineBucket

fun summarizeDefaultTransitTimeManifestsForCycle(items: MutableList<DefaultTransitTimeManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun derivePendingNodeCyclesFromManifest(items: MutableList<DefaultTransitTimeManifest>): MutableList<PendingNodeCycle> {
    val out = mutableListOf<PendingNodeCycle>()
    for (item in items) {
        val derived = newPendingNodeCycle(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balancePendingNodeCycle(item: PendingNodeCycle): String = transitTimeBaselineBucket(item.weightedLoad())
