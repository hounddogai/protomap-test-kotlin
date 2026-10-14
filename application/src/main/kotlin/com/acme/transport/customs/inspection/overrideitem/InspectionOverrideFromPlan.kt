package com.acme.transport.customs.inspection.overrideitem

import com.acme.transport.customs.clearance.segment.ClearancePlan
import com.acme.transport.customs.clearance.segment.clearanceMilestoneBucket

fun summarizeClearancePlansForOverride(items: MutableList<ClearancePlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveInspectionOverridesFromPlan(items: MutableList<ClearancePlan>): MutableList<InspectionOverride> {
    val out = mutableListOf<InspectionOverride>()
    for (item in items) {
        val derived = newInspectionOverride(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileInspectionOverride(item: InspectionOverride): String = clearanceMilestoneBucket(item.measuredValue())
