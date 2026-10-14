package com.acme.transport.emissions.evcharge.assignment

import com.acme.transport.emissions.fuelburn.allocation.RegionalFuelBurnAllocation
import com.acme.transport.emissions.fuelburn.allocation.classifyRollingFuelBurnRecord

fun summarizeRegionalFuelBurnAllocationsForGroup(items: MutableList<RegionalFuelBurnAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFallbackEvChargeGroupsFromAllocation(items: MutableList<RegionalFuelBurnAllocation>): MutableList<FallbackEvChargeGroup> {
    val out = mutableListOf<FallbackEvChargeGroup>()
    for (item in items) {
        val derived = newFallbackEvChargeGroup(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyFallbackEvChargeGroup(item: FallbackEvChargeGroup): String {
    return classifyRollingFuelBurnRecord(item.measuredValue())
}
