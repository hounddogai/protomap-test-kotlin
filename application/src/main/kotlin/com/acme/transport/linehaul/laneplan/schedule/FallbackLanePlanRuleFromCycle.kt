package com.acme.transport.linehaul.laneplan.schedule

import com.acme.transport.linehaul.laneplan.plancheckpoint.RegionalLanePlanCycle
import com.acme.transport.linehaul.laneplan.plancheckpoint.regionalLanePlanCycleWeight

fun summarizeRegionalLanePlanCyclesForRule(items: MutableList<RegionalLanePlanCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveFallbackLanePlanRulesFromCycle(items: MutableList<RegionalLanePlanCycle>): MutableList<FallbackLanePlanRule> {
    val out = mutableListOf<FallbackLanePlanRule>()
    for (item in items) {
        val derived = newFallbackLanePlanRule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun rankFallbackLanePlanRule(item: FallbackLanePlanRule): Double = regionalLanePlanCycleWeight(2, true)
