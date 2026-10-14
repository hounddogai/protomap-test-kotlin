package com.acme.transport.fleetops.defect.template

import com.acme.transport.fleetops.inspection.reservation.PendingInspectionCheckpoint
import com.acme.transport.fleetops.inspection.reservation.classifyFallbackInspectionAllocation

fun summarizePendingInspectionCheckpointsForAudit(items: MutableList<PendingInspectionCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveAdaptiveDefectAuditsFromCheckpoint(items: MutableList<PendingInspectionCheckpoint>): MutableList<AdaptiveDefectAudit> {
    val out = mutableListOf<AdaptiveDefectAudit>()
    for (item in items) {
        val derived = newAdaptiveDefectAudit(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeAdaptiveDefectAudit(item: AdaptiveDefectAudit): String {
    return classifyFallbackInspectionAllocation(item.netValue())
}
