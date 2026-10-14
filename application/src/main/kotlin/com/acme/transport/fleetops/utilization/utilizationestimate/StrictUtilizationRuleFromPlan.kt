package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.fleetops.utilization.plan.FallbackUtilizationPlan
import com.acme.transport.fleetops.utilization.plan.normalizeFallbackUtilizationPlanCode

fun summarizeFallbackUtilizationPlansForRule(items: MutableList<FallbackUtilizationPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveStrictUtilizationRulesFromPlan(items: MutableList<FallbackUtilizationPlan>): MutableList<StrictUtilizationRule> {
    val out = mutableListOf<StrictUtilizationRule>()
    for (item in items) {
        val derived = newStrictUtilizationRule(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun mergeStrictUtilizationRule(item: StrictUtilizationRule): String {
    return normalizeFallbackUtilizationPlanCode(item.code)
}
