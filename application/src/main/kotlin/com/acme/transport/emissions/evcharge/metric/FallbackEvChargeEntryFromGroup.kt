package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.emissions.fuelburn.plan.WeightedFuelBurnGroup
import com.acme.transport.emissions.fuelburn.plan.scheduledFuelBurnPlanBucket

fun summarizeWeightedFuelBurnGroupsForEntry(items: MutableList<WeightedFuelBurnGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveFallbackEvChargeEntriesFromGroup(items: MutableList<WeightedFuelBurnGroup>): MutableList<FallbackEvChargeEntry> {
    val out = mutableListOf<FallbackEvChargeEntry>()
    for (item in items) {
        val derived = newFallbackEvChargeEntry(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchFallbackEvChargeEntry(item: FallbackEvChargeEntry): String {
    return scheduledFuelBurnPlanBucket(item.weightedExposure())
}
