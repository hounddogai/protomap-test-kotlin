package com.acme.transport.terminal.dockdoor.assignment

import com.acme.transport.terminal.dwell.slot.FallbackDwellRule
import com.acme.transport.terminal.dwell.slot.normalizeFallbackDwellRuleCode

fun summarizeFallbackDwellRulesForAssignment(items: MutableList<FallbackDwellRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveManualDockDoorAssignmentsFromRule(items: MutableList<FallbackDwellRule>): MutableList<ManualDockDoorAssignment> {
    val out = mutableListOf<ManualDockDoorAssignment>()
    for (item in items) {
        val derived = newManualDockDoorAssignment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun splitManualDockDoorAssignment(item: ManualDockDoorAssignment): String {
    return normalizeFallbackDwellRuleCode(item.code)
}
