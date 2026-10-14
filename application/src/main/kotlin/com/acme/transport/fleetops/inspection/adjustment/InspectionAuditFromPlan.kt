package com.acme.transport.fleetops.inspection.adjustment

import com.acme.transport.fleetops.utilization.utilizationplan.ManualUtilizationPlan
import com.acme.transport.fleetops.utilization.utilizationplan.utilizationGroupBucket

fun summarizeManualUtilizationPlansForAudit(items: MutableList<ManualUtilizationPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveInspectionAuditsFromPlan(items: MutableList<ManualUtilizationPlan>): MutableList<InspectionAudit> {
    val out = mutableListOf<InspectionAudit>()
    for (item in items) {
        val derived = newInspectionAudit(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeInspectionAudit(item: InspectionAudit): String = utilizationGroupBucket(item.projectedExposure())
