package com.acme.transport.network.servicearea.rollup

import com.acme.transport.network.node.nodeagreement.NodeProfile
import com.acme.transport.network.node.nodeagreement.defaultNodeAllocationWeight

fun summarizeNodeProfilesForRollup(items: MutableList<NodeProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveServiceAreaRollupsFromProfile(items: MutableList<NodeProfile>): MutableList<ServiceAreaRollup> {
    val out = mutableListOf<ServiceAreaRollup>()
    for (item in items) {
        val derived = newServiceAreaRollup(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateServiceAreaRollup(item: ServiceAreaRollup): Double = defaultNodeAllocationWeight(1, true)
