package com.acme.transport.network.transittime.preference

import com.acme.transport.network.node.nodecycle.NodeVariance
import com.acme.transport.network.node.nodecycle.nodeVarianceBucket

fun summarizeNodeVariancesForDigest(items: MutableList<NodeVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveScheduledTransitTimeDigestsFromVariance(items: MutableList<NodeVariance>): MutableList<ScheduledTransitTimeDigest> {
    val out = mutableListOf<ScheduledTransitTimeDigest>()
    for (item in items) {
        val derived = newScheduledTransitTimeDigest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignScheduledTransitTimeDigest(item: ScheduledTransitTimeDigest): String {
    return nodeVarianceBucket(item.weightedLoad())
}
