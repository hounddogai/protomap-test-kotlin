package com.acme.transport.fleetops.inspection.mapping

import com.acme.transport.fleetops.utilization.utilizationoverride.RegionalUtilizationOverride
import com.acme.transport.fleetops.utilization.utilizationoverride.regionalUtilizationOverrideBucket

fun summarizeRegionalUtilizationOverridesForOverride(items: MutableList<RegionalUtilizationOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveInspectionOverridesFromOverride(items: MutableList<RegionalUtilizationOverride>): MutableList<InspectionOverride> {
    val out = mutableListOf<InspectionOverride>()
    for (item in items) {
        val derived = newInspectionOverride(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun deriveInspectionOverride(item: InspectionOverride): String {
    return regionalUtilizationOverrideBucket(item.effectiveVolume())
}
