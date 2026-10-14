package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.fleetops.inspection.adjustment.InspectionAdjustment
import com.acme.transport.fleetops.inspection.adjustment.normalizeInspectionAdjustmentCode

fun summarizeInspectionAdjustmentsForThreshold(items: MutableList<InspectionAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveDefectThresholdsFromAdjustment(items: MutableList<InspectionAdjustment>): MutableList<DefectThreshold> {
    val out = mutableListOf<DefectThreshold>()
    for (item in items) {
        val derived = newDefectThreshold(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateDefectThreshold(item: DefectThreshold): String = normalizeInspectionAdjustmentCode(item.code)
