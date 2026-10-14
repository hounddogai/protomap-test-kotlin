package com.acme.transport.fleetops.leasereturn.baseline

import com.acme.transport.fleetops.utilization.utilizationplan.UtilizationGroup
import com.acme.transport.fleetops.utilization.utilizationplan.manualUtilizationPlanBucket

fun summarizeUtilizationGroupsForSchedule(items: MutableList<UtilizationGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveAdaptiveLeaseReturnSchedulesFromGroup(items: MutableList<UtilizationGroup>): MutableList<AdaptiveLeaseReturnSchedule> {
    val out = mutableListOf<AdaptiveLeaseReturnSchedule>()
    for (item in items) {
        val derived = newAdaptiveLeaseReturnSchedule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceAdaptiveLeaseReturnSchedule(item: AdaptiveLeaseReturnSchedule): String {
    return manualUtilizationPlanBucket(item.adjustedTotal())
}
