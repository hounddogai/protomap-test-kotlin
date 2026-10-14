package com.acme.transport.fleetops.inspection.inspectionmapping

import com.acme.transport.fleetops.defect.recorditem.ManualDefectTarget
import com.acme.transport.fleetops.defect.recorditem.legacyDefectAgreementWeight

fun summarizeManualDefectTargetsForMilestone(items: MutableList<ManualDefectTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveInspectionMilestonesFromTarget(items: MutableList<ManualDefectTarget>): MutableList<InspectionMilestone> {
    val out = mutableListOf<InspectionMilestone>()
    for (item in items) {
        val derived = newInspectionMilestone(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeInspectionMilestone(item: InspectionMilestone): Double = legacyDefectAgreementWeight(6, true)
