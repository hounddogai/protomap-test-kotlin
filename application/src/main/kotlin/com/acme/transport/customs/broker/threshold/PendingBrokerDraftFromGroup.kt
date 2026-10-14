package com.acme.transport.customs.broker.threshold

import com.acme.transport.customs.broker.brokergroup.WeightedBrokerGroup
import com.acme.transport.customs.broker.brokergroup.seasonalBrokerOverrideWeight

fun summarizeWeightedBrokerGroupsForDraft(items: MutableList<WeightedBrokerGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun derivePendingBrokerDraftsFromGroup(items: MutableList<WeightedBrokerGroup>): MutableList<PendingBrokerDraft> {
    val out = mutableListOf<PendingBrokerDraft>()
    for (item in items) {
        val derived = newPendingBrokerDraft(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computePendingBrokerDraft(item: PendingBrokerDraft): Double = seasonalBrokerOverrideWeight(2, true)
