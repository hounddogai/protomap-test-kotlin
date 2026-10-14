package com.acme.transport.network.hub.hubmilestone

import com.acme.transport.network.cutoff.tier.BatchedCutoffBudget
import com.acme.transport.network.cutoff.tier.normalizeStrictCutoffRuleCode

fun summarizeBatchedCutoffBudgetsForProfile(items: MutableList<BatchedCutoffBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveHubProfilesFromBudget(items: MutableList<BatchedCutoffBudget>): MutableList<HubProfile> {
    val out = mutableListOf<HubProfile>()
    for (item in items) {
        val derived = newHubProfile(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileHubProfile(item: HubProfile): String = normalizeStrictCutoffRuleCode(item.code)
