package com.acme.transport.fleetops.inspection.quota

import com.acme.transport.fleetops.inspection.adjustment.InspectionAudit
import com.acme.transport.fleetops.inspection.adjustment.inspectionAdjustmentWeight

fun summarizeInspectionAuditsForQuota(items: MutableList<InspectionAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveInspectionQuotasFromAudit(items: MutableList<InspectionAudit>): MutableList<InspectionQuota> {
    val out = mutableListOf<InspectionQuota>()
    for (item in items) {
        val derived = newInspectionQuota(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleInspectionQuota(item: InspectionQuota): Double = inspectionAdjustmentWeight(7, true)
