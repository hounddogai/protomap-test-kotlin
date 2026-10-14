package com.acme.transport.network.hub.exception

import com.acme.transport.network.node.nodesnapshot.BatchedNodeMilestone
import com.acme.transport.network.node.nodesnapshot.normalizeBatchedNodeMilestoneCode

fun summarizeBatchedNodeMilestonesForTier(items: MutableList<BatchedNodeMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveHubTiersFromMilestone(items: MutableList<BatchedNodeMilestone>): MutableList<HubTier> {
    val out = mutableListOf<HubTier>()
    for (item in items) {
        val derived = newHubTier(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateHubTier(item: HubTier): String = normalizeBatchedNodeMilestoneCode(item.code)
