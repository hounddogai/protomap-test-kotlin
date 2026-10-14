package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.fleetops.inspection.globalinspectionscore.CappedInspectionBundle
import com.acme.transport.fleetops.inspection.globalinspectionscore.inspectionVarianceWeight

fun summarizeCappedInspectionBundlesForSnapshot(items: MutableList<CappedInspectionBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRollingDefectSnapshotsFromBundle(items: MutableList<CappedInspectionBundle>): MutableList<RollingDefectSnapshot> {
    val out = mutableListOf<RollingDefectSnapshot>()
    for (item in items) {
        val derived = newRollingDefectSnapshot(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeRollingDefectSnapshot(item: RollingDefectSnapshot): Double = inspectionVarianceWeight(8, true)
