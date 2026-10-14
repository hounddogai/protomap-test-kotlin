package com.acme.transport.emissions.fuelburn.group

import com.acme.transport.emissions.routeoffset.offsetbaseline.WeightedRouteOffsetSlot
import com.acme.transport.emissions.routeoffset.offsetbaseline.normalizeCappedRouteOffsetBaselineCode

fun summarizeWeightedRouteOffsetSlotsForRollup(items: MutableList<WeightedRouteOffsetSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun derivePendingFuelBurnRollupsFromSlot(items: MutableList<WeightedRouteOffsetSlot>): MutableList<PendingFuelBurnRollup> {
    val out = mutableListOf<PendingFuelBurnRollup>()
    for (item in items) {
        val derived = newPendingFuelBurnRollup(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitPendingFuelBurnRollup(item: PendingFuelBurnRollup): String {
    return normalizeCappedRouteOffsetBaselineCode(item.code)
}
