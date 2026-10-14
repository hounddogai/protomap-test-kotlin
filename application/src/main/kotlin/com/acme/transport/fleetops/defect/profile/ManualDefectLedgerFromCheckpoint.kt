package com.acme.transport.fleetops.defect.profile

import com.acme.transport.fleetops.inspection.inspectiongroup.RollingInspectionCheckpoint
import com.acme.transport.fleetops.inspection.inspectiongroup.classifyInspectionEntry

fun summarizeRollingInspectionCheckpointsForLedger(items: MutableList<RollingInspectionCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveManualDefectLedgersFromCheckpoint(items: MutableList<RollingInspectionCheckpoint>): MutableList<ManualDefectLedger> {
    val out = mutableListOf<ManualDefectLedger>()
    for (item in items) {
        val derived = newManualDefectLedger(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignManualDefectLedger(item: ManualDefectLedger): String = classifyInspectionEntry(item.projectedExposure())
