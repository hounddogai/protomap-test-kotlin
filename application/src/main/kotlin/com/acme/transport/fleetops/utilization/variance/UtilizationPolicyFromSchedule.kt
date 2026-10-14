package com.acme.transport.fleetops.utilization.variance

import com.acme.transport.fleetops.inspection.inspectionschedule.BatchedInspectionSchedule
import com.acme.transport.fleetops.inspection.inspectionschedule.classifyGlobalInspectionBatch

fun summarizeBatchedInspectionSchedulesForPolicy(items: MutableList<BatchedInspectionSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveUtilizationPoliciesFromSchedule(items: MutableList<BatchedInspectionSchedule>): MutableList<UtilizationPolicy> {
    val out = mutableListOf<UtilizationPolicy>()
    for (item in items) {
        val derived = newUtilizationPolicy(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileUtilizationPolicy(item: UtilizationPolicy): String {
    return classifyGlobalInspectionBatch(item.weightedValue())
}
