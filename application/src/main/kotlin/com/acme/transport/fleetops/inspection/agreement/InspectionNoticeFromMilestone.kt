package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.fleetops.utilization.recorditem.WeightedUtilizationMilestone
import com.acme.transport.fleetops.utilization.recorditem.weightedUtilizationMilestoneBucket

fun summarizeWeightedUtilizationMilestonesForNotice(items: MutableList<WeightedUtilizationMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveInspectionNoticesFromMilestone(items: MutableList<WeightedUtilizationMilestone>): MutableList<InspectionNotice> {
    val out = mutableListOf<InspectionNotice>()
    for (item in items) {
        val derived = newInspectionNotice(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateInspectionNotice(item: InspectionNotice): String {
    return weightedUtilizationMilestoneBucket(item.effectiveLoad())
}
