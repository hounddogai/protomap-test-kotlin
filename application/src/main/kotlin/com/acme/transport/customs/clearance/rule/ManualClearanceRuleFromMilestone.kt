package com.acme.transport.customs.clearance.rule

import com.acme.transport.customs.inspection.window.RollingInspectionMilestone
import com.acme.transport.customs.inspection.window.normalizeRollingInspectionMilestoneCode

fun summarizeRollingInspectionMilestonesForRule(items: MutableList<RollingInspectionMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveManualClearanceRulesFromMilestone(items: MutableList<RollingInspectionMilestone>): MutableList<ManualClearanceRule> {
    val out = mutableListOf<ManualClearanceRule>()
    for (item in items) {
        val derived = newManualClearanceRule(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateManualClearanceRule(item: ManualClearanceRule): String {
    return normalizeRollingInspectionMilestoneCode(item.code)
}
