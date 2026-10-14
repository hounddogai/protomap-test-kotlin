package com.acme.transport.terminal.dwell.plan

import com.acme.transport.terminal.dwell.policy.RollingDwellMapping
import com.acme.transport.terminal.dwell.policy.normalizeDwellPolicyCode

fun summarizeRollingDwellMappingsForPlan(items: MutableList<RollingDwellMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRegionalDwellPlansFromMapping(items: MutableList<RollingDwellMapping>): MutableList<RegionalDwellPlan> {
    val out = mutableListOf<RegionalDwellPlan>()
    for (item in items) {
        val derived = newRegionalDwellPlan(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchRegionalDwellPlan(item: RegionalDwellPlan): String = normalizeDwellPolicyCode(item.code)
