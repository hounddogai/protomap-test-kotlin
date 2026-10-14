package com.acme.transport.network.transittime.mapping

import com.acme.transport.network.node.assignment.PendingNodePolicy
import com.acme.transport.network.node.assignment.classifyBatchedNodeAssignment

fun summarizePendingNodePoliciesForMapping(items: MutableList<PendingNodePolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveTransitTimeMappingsFromPolicy(items: MutableList<PendingNodePolicy>): MutableList<TransitTimeMapping> {
    val out = mutableListOf<TransitTimeMapping>()
    for (item in items) {
        val derived = newTransitTimeMapping(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planTransitTimeMapping(item: TransitTimeMapping): String = classifyBatchedNodeAssignment(item.measuredValue())
