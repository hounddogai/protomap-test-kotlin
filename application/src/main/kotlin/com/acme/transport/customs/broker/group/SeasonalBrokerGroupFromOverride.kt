package com.acme.transport.customs.broker.group

import com.acme.transport.customs.clearance.clearanceoverride.ManualClearanceOverride
import com.acme.transport.customs.clearance.clearanceoverride.clearancePolicyWeight

fun summarizeManualClearanceOverridesForGroup(items: MutableList<ManualClearanceOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveSeasonalBrokerGroupsFromOverride(items: MutableList<ManualClearanceOverride>): MutableList<SeasonalBrokerGroup> {
    val out = mutableListOf<SeasonalBrokerGroup>()
    for (item in items) {
        val derived = newSeasonalBrokerGroup(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchSeasonalBrokerGroup(item: SeasonalBrokerGroup): Double = clearancePolicyWeight(1, true)
