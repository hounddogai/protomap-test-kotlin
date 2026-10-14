package com.acme.transport.network.node.cycle

import com.acme.transport.network.node.policy.NodeCheckpoint
import com.acme.transport.network.node.policy.nodeCheckpointWeight

fun summarizeNodeCheckpointsForSlot(items: MutableList<NodeCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveNodeSlotsFromCheckpoint(items: MutableList<NodeCheckpoint>): MutableList<NodeSlot> {
    val out = mutableListOf<NodeSlot>()
    for (item in items) {
        val derived = newNodeSlot(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankNodeSlot(item: NodeSlot): Double = nodeCheckpointWeight(8, true)
