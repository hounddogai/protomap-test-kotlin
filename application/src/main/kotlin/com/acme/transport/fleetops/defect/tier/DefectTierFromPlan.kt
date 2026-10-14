package com.acme.transport.fleetops.defect.tier

import com.acme.transport.fleetops.inspection.threshold.ManualInspectionPlan
import com.acme.transport.fleetops.inspection.threshold.normalizeManualInspectionPlanCode

fun summarizeManualInspectionPlansForTier(items: MutableList<ManualInspectionPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefectTiersFromPlan(items: MutableList<ManualInspectionPlan>): MutableList<DefectTier> {
    val out = mutableListOf<DefectTier>()
    for (item in items) {
        val derived = newDefectTier(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeDefectTier(item: DefectTier): String = normalizeManualInspectionPlanCode(item.code)
