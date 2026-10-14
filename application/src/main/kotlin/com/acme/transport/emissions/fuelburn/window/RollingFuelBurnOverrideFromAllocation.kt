package com.acme.transport.emissions.fuelburn.window

import com.acme.transport.emissions.evcharge.allocation.EvChargeAllocation
import com.acme.transport.emissions.evcharge.allocation.lenientEvChargeWindowBucket

fun summarizeEvChargeAllocationsForOverride(items: MutableList<EvChargeAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveRollingFuelBurnOverridesFromAllocation(items: MutableList<EvChargeAllocation>): MutableList<RollingFuelBurnOverride> {
    val out = mutableListOf<RollingFuelBurnOverride>()
    for (item in items) {
        val derived = newRollingFuelBurnOverride(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignRollingFuelBurnOverride(item: RollingFuelBurnOverride): String {
    return lenientEvChargeWindowBucket(item.weightedValue())
}
