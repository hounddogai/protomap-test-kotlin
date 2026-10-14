package com.acme.transport.network.node.nodescore

import com.acme.transport.network.hub.hubsummary.DefaultHubTier
import com.acme.transport.network.hub.hubsummary.normalizeDefaultHubTierCode

fun summarizeDefaultHubTiersForRule(items: MutableList<DefaultHubTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveNodeRulesFromTier(items: MutableList<DefaultHubTier>): MutableList<NodeRule> {
    val out = mutableListOf<NodeRule>()
    for (item in items) {
        val derived = newNodeRule(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun settleNodeRule(item: NodeRule): String = normalizeDefaultHubTierCode(item.code)
