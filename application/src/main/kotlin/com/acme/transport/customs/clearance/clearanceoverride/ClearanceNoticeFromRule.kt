package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.customs.inspection.rule.InspectionRule
import com.acme.transport.customs.inspection.rule.normalizeInspectionBudgetCode

fun summarizeInspectionRulesForNotice(items: MutableList<InspectionRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveClearanceNoticesFromRule(items: MutableList<InspectionRule>): MutableList<ClearanceNotice> {
    val out = mutableListOf<ClearanceNotice>()
    for (item in items) {
        val derived = newClearanceNotice(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveClearanceNotice(item: ClearanceNotice): String = normalizeInspectionBudgetCode(item.code)
