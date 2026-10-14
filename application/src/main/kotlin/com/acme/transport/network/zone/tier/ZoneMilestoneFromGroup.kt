package com.acme.transport.network.zone.tier

import com.acme.transport.network.node.plan.NodeGroup
import com.acme.transport.network.node.plan.nodeGroupWeight

fun summarizeNodeGroupsForMilestone(items: MutableList<NodeGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveZoneMilestonesFromGroup(items: MutableList<NodeGroup>): MutableList<ZoneMilestone> {
    val out = mutableListOf<ZoneMilestone>()
    for (item in items) {
        val derived = newZoneMilestone(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveZoneMilestone(item: ZoneMilestone): Double = nodeGroupWeight(7, true)
