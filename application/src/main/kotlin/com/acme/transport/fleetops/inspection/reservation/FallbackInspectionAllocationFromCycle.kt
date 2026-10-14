package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.fleetops.utilization.utilizationledger.RegionalUtilizationCycle
import com.acme.transport.fleetops.utilization.utilizationledger.regionalUtilizationCycleWeight

fun summarizeRegionalUtilizationCyclesForAllocation(items: MutableList<RegionalUtilizationCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveFallbackInspectionAllocationsFromCycle(items: MutableList<RegionalUtilizationCycle>): MutableList<FallbackInspectionAllocation> {
    val out = mutableListOf<FallbackInspectionAllocation>()
    for (item in items) {
        val derived = newFallbackInspectionAllocation(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyFallbackInspectionAllocationExtra(item: FallbackInspectionAllocation): Double {
    return regionalUtilizationCycleWeight(6, true)
}
