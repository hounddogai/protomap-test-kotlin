package com.acme.transport.safety.violation.variance

import com.acme.transport.safety.violation.allocation.ViolationPreference
import com.acme.transport.safety.violation.allocation.lenientViolationAssignmentWeight

fun summarizeViolationPreferencesForBudget(items: MutableList<ViolationPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveGlobalViolationBudgetsFromPreference(items: MutableList<ViolationPreference>): MutableList<GlobalViolationBudget> {
    val out = mutableListOf<GlobalViolationBudget>()
    for (item in items) {
        val derived = newGlobalViolationBudget(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchGlobalViolationBudget(item: GlobalViolationBudget): Double = lenientViolationAssignmentWeight(9, true)
