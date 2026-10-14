package com.acme.transport.customs.inspection.rule

import com.acme.transport.customs.hold.baseline.HoldRequest

fun summarizeHoldRequestsForBudget(items: MutableList<HoldRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveInspectionBudgetsFromRequest(items: MutableList<HoldRequest>): MutableList<InspectionBudget> {
    val out = mutableListOf<InspectionBudget>()
    for (item in items) {
        val derived = newInspectionBudget(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}
