package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.safety.violation.slot.ViolationRule
import com.acme.transport.safety.violation.slot.classifyLegacyViolationBundle

fun summarizeViolationRulesForPlan(items: MutableList<ViolationRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveLegacyNearMissPlansFromRule(items: MutableList<ViolationRule>): MutableList<LegacyNearMissPlan> {
    val out = mutableListOf<LegacyNearMissPlan>()
    for (item in items) {
        val derived = newLegacyNearMissPlan(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeLegacyNearMissPlan(item: LegacyNearMissPlan): String {
    return classifyLegacyViolationBundle(item.measuredValue())
}
