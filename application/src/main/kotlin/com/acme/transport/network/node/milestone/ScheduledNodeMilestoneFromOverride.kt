package com.acme.transport.network.node.milestone

import com.acme.transport.network.node.cycle.NodeOverride
import com.acme.transport.network.node.cycle.nodeDigestWeight

fun summarizeNodeOverridesForMilestone(items: MutableList<NodeOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveScheduledNodeMilestonesFromOverride(items: MutableList<NodeOverride>): MutableList<ScheduledNodeMilestone> {
    val out = mutableListOf<ScheduledNodeMilestone>()
    for (item in items) {
        val derived = newScheduledNodeMilestone(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeScheduledNodeMilestone(item: ScheduledNodeMilestone): Double = nodeDigestWeight(7, true)
