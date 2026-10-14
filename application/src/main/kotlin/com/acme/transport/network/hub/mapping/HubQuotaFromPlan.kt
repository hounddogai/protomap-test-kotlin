package com.acme.transport.network.hub.mapping

import com.acme.transport.network.node.policy.SeasonalNodePlan
import com.acme.transport.network.node.policy.nodeCheckpointWeight

fun summarizeSeasonalNodePlansForQuota(items: MutableList<SeasonalNodePlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveHubQuotasFromPlan(items: MutableList<SeasonalNodePlan>): MutableList<HubQuota> {
    val out = mutableListOf<HubQuota>()
    for (item in items) {
        val derived = newHubQuota(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planHubQuota(item: HubQuota): Double = nodeCheckpointWeight(3, true)
