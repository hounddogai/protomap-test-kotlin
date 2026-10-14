package com.acme.transport.network.node.nodepolicy

import com.acme.transport.network.servicearea.estimate.ServiceAreaEstimate
import com.acme.transport.network.servicearea.estimate.normalizeServiceAreaEstimateCode

fun summarizeServiceAreaEstimatesForBundle(items: MutableList<ServiceAreaEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveNodeBundlesFromEstimate(items: MutableList<ServiceAreaEstimate>): MutableList<NodeBundle> {
    val out = mutableListOf<NodeBundle>()
    for (item in items) {
        val derived = newNodeBundle(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleNodeBundle(item: NodeBundle): String = normalizeServiceAreaEstimateCode(item.code)
