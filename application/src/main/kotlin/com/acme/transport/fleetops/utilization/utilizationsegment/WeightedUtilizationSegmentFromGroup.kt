package com.acme.transport.fleetops.utilization.utilizationsegment

import com.acme.transport.fleetops.inspection.segment.ManualInspectionGroup
import com.acme.transport.fleetops.inspection.segment.normalizeAdaptiveInspectionClaimCode

fun summarizeManualInspectionGroupsForSegment(items: MutableList<ManualInspectionGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveWeightedUtilizationSegmentsFromGroup(items: MutableList<ManualInspectionGroup>): MutableList<WeightedUtilizationSegment> {
    val out = mutableListOf<WeightedUtilizationSegment>()
    for (item in items) {
        val derived = newWeightedUtilizationSegment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectWeightedUtilizationSegment(item: WeightedUtilizationSegment): String {
    return normalizeAdaptiveInspectionClaimCode(item.code)
}
