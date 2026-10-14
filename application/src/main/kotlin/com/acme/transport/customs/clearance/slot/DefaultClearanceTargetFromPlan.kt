package com.acme.transport.customs.clearance.slot

import com.acme.transport.customs.inspection.inspectionplan.LenientInspectionPlan
import com.acme.transport.customs.inspection.inspectionplan.lenientInspectionPlanWeight

fun summarizeLenientInspectionPlansForTarget(items: MutableList<LenientInspectionPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveDefaultClearanceTargetsFromPlan(items: MutableList<LenientInspectionPlan>): MutableList<DefaultClearanceTarget> {
    val out = mutableListOf<DefaultClearanceTarget>()
    for (item in items) {
        val derived = newDefaultClearanceTarget(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeDefaultClearanceTarget(item: DefaultClearanceTarget): Double = lenientInspectionPlanWeight(3, true)
