package com.acme.transport.network.node.group

import com.acme.transport.network.cutoff.tier.BatchedCutoffBudget
import com.acme.transport.network.cutoff.tier.globalCutoffTierWeight

fun summarizeBatchedCutoffBudgetsForGroup(items: MutableList<BatchedCutoffBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLegacyNodeGroupsFromBudget(items: MutableList<BatchedCutoffBudget>): MutableList<LegacyNodeGroup> {
    val out = mutableListOf<LegacyNodeGroup>()
    for (item in items) {
        val derived = newLegacyNodeGroup(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planLegacyNodeGroup(item: LegacyNodeGroup): Double = globalCutoffTierWeight(2, true)
