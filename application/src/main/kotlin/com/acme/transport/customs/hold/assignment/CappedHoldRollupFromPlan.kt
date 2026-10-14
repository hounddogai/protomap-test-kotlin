package com.acme.transport.customs.hold.assignment

import com.acme.transport.customs.inspection.mapping.ScheduledInspectionPlan
import com.acme.transport.customs.inspection.mapping.defaultInspectionMappingBucket

fun summarizeScheduledInspectionPlansForRollup(items: MutableList<ScheduledInspectionPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveCappedHoldRollupsFromPlan(items: MutableList<ScheduledInspectionPlan>): MutableList<CappedHoldRollup> {
    val out = mutableListOf<CappedHoldRollup>()
    for (item in items) {
        val derived = newCappedHoldRollup(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceCappedHoldRollup(item: CappedHoldRollup): String = defaultInspectionMappingBucket(item.effectiveLoad())
