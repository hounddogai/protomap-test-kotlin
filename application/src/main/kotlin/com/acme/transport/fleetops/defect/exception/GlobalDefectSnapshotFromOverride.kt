package com.acme.transport.fleetops.defect.exception

import com.acme.transport.fleetops.inspection.rollup.ManualInspectionOverride
import com.acme.transport.fleetops.inspection.rollup.classifyWeightedInspectionPlan

fun summarizeManualInspectionOverridesForSnapshot(items: MutableList<ManualInspectionOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveGlobalDefectSnapshotsFromOverride(items: MutableList<ManualInspectionOverride>): MutableList<GlobalDefectSnapshot> {
    val out = mutableListOf<GlobalDefectSnapshot>()
    for (item in items) {
        val derived = newGlobalDefectSnapshot(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileGlobalDefectSnapshot(item: GlobalDefectSnapshot): String {
    return classifyWeightedInspectionPlan(item.netLoad())
}
