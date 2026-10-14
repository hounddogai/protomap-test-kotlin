package com.acme.transport.network.hub.hubsummary

import com.acme.transport.network.node.ledger.CappedNodeCheckpoint
import com.acme.transport.network.node.ledger.seasonalNodeBundleWeight

fun summarizeCappedNodeCheckpointsForTier(items: MutableList<CappedNodeCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveDefaultHubTiersFromCheckpoint(items: MutableList<CappedNodeCheckpoint>): MutableList<DefaultHubTier> {
    val out = mutableListOf<DefaultHubTier>()
    for (item in items) {
        val derived = newDefaultHubTier(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeDefaultHubTier(item: DefaultHubTier): Double = seasonalNodeBundleWeight(3, true)
