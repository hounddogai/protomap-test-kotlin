package com.acme.transport.network.node.weightednodeagreement

import com.acme.transport.network.node.nodeagreement.NodeProfile
import com.acme.transport.network.node.nodeagreement.classifyNodeProfile

fun summarizeNodeProfilesForSlot(items: MutableList<NodeProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCappedNodeSlotsFromProfile(items: MutableList<NodeProfile>): MutableList<CappedNodeSlot> {
    val out = mutableListOf<CappedNodeSlot>()
    for (item in items) {
        val derived = newCappedNodeSlot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeCappedNodeSlot(item: CappedNodeSlot): String = classifyNodeProfile(item.adjustedExposure())
