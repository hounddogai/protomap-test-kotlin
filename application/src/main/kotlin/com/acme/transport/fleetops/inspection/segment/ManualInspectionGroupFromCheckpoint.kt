package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.fleetops.defect.defectcheckpoint.DefaultDefectCheckpoint
import com.acme.transport.fleetops.defect.defectcheckpoint.tieredDefectAuditBucket

fun summarizeDefaultDefectCheckpointsForGroup(items: MutableList<DefaultDefectCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveManualInspectionGroupsFromCheckpoint(items: MutableList<DefaultDefectCheckpoint>): MutableList<ManualInspectionGroup> {
    val out = mutableListOf<ManualInspectionGroup>()
    for (item in items) {
        val derived = newManualInspectionGroup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileManualInspectionGroup(item: ManualInspectionGroup): String {
    return tieredDefectAuditBucket(item.adjustedTotal())
}
