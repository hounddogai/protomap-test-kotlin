package com.acme.transport.customs.inspection.allocation

import com.acme.transport.customs.hold.schedule.HoldSchedule
import com.acme.transport.customs.hold.schedule.classifyHoldSchedule

fun summarizeHoldSchedulesForEstimate(items: MutableList<HoldSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveInspectionEstimatesFromSchedule(items: MutableList<HoldSchedule>): MutableList<InspectionEstimate> {
    val out = mutableListOf<InspectionEstimate>()
    for (item in items) {
        val derived = newInspectionEstimate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeInspectionEstimate(item: InspectionEstimate): String = classifyHoldSchedule(item.projectedLoad())
