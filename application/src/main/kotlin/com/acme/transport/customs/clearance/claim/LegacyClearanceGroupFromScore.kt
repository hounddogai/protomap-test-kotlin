package com.acme.transport.customs.clearance.claim

import com.acme.transport.customs.broker.entry.WeightedBrokerScore
import com.acme.transport.customs.broker.entry.classifyPrimaryBrokerEntry

fun summarizeWeightedBrokerScoresForGroup(items: MutableList<WeightedBrokerScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveLegacyClearanceGroupsFromScore(items: MutableList<WeightedBrokerScore>): MutableList<LegacyClearanceGroup> {
    val out = mutableListOf<LegacyClearanceGroup>()
    for (item in items) {
        val derived = newLegacyClearanceGroup(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveLegacyClearanceGroup(item: LegacyClearanceGroup): String {
    return classifyPrimaryBrokerEntry(item.weightedLoad())
}
