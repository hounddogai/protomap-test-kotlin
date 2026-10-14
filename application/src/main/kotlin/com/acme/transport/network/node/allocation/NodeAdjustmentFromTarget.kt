package com.acme.transport.network.node.allocation

import com.acme.transport.network.servicearea.target.ServiceAreaTarget
import com.acme.transport.network.servicearea.target.normalizeServiceAreaProposalCode

fun summarizeServiceAreaTargetsForAdjustment(items: MutableList<ServiceAreaTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveNodeAdjustmentsFromTarget(items: MutableList<ServiceAreaTarget>): MutableList<NodeAdjustment> {
    val out = mutableListOf<NodeAdjustment>()
    for (item in items) {
        val derived = newNodeAdjustment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeNodeAdjustment(item: NodeAdjustment): String = normalizeServiceAreaProposalCode(item.code)
