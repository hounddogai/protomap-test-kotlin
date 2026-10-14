package com.acme.transport.fleetops.inspection.template

import com.acme.transport.fleetops.defect.defectrecord.PrimaryDefectTarget
import com.acme.transport.fleetops.defect.defectrecord.normalizeTieredDefectRecordCode

fun summarizePrimaryDefectTargetsForAdjustment(items: MutableList<PrimaryDefectTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveRegionalInspectionAdjustmentsFromTarget(items: MutableList<PrimaryDefectTarget>): MutableList<RegionalInspectionAdjustment> {
    val out = mutableListOf<RegionalInspectionAdjustment>()
    for (item in items) {
        val derived = newRegionalInspectionAdjustment(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyRegionalInspectionAdjustment(item: RegionalInspectionAdjustment): String {
    return normalizeTieredDefectRecordCode(item.code)
}
