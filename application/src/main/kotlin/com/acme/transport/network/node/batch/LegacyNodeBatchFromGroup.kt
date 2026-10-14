package com.acme.transport.network.node.batch

import com.acme.transport.network.node.group.LegacyNodeGroup
import com.acme.transport.network.node.group.weightedNodeThresholdBucket

fun summarizeLegacyNodeGroupsForBatch(items: MutableList<LegacyNodeGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveLegacyNodeBatchesFromGroup(items: MutableList<LegacyNodeGroup>): MutableList<LegacyNodeBatch> {
    val out = mutableListOf<LegacyNodeBatch>()
    for (item in items) {
        val derived = newLegacyNodeBatch(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveLegacyNodeBatch(item: LegacyNodeBatch): String = weightedNodeThresholdBucket(item.effectiveVolume())
