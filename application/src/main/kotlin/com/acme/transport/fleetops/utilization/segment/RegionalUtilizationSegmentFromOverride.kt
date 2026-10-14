package com.acme.transport.fleetops.utilization.segment

import com.acme.transport.fleetops.inspection.mapping.InspectionOverride
import com.acme.transport.fleetops.inspection.mapping.inspectionRequestWeight

fun summarizeInspectionOverridesForSegment(items: MutableList<InspectionOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveRegionalUtilizationSegmentsFromOverride(items: MutableList<InspectionOverride>): MutableList<RegionalUtilizationSegment> {
    val out = mutableListOf<RegionalUtilizationSegment>()
    for (item in items) {
        val derived = newRegionalUtilizationSegment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeRegionalUtilizationSegment(item: RegionalUtilizationSegment): Double {
    return inspectionRequestWeight(7, true)
}
