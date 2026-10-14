package com.acme.transport.crewing.swap.group

import com.acme.transport.crewing.roster.rosterforecast.WeightedRosterTier
import com.acme.transport.crewing.roster.rosterforecast.classifyRollingRosterEntry

fun summarizeWeightedRosterTiersForGroup(items: MutableList<WeightedRosterTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveSwapGroupsFromTier(items: MutableList<WeightedRosterTier>): MutableList<SwapGroup> {
    val out = mutableListOf<SwapGroup>()
    for (item in items) {
        val derived = newSwapGroup(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceSwapGroup(item: SwapGroup): String = classifyRollingRosterEntry(item.effectiveTotal())
