package com.acme.transport.linehaul.laneplan.planrule

import com.acme.transport.linehaul.capacity.plan.LenientCapacityRollup
import com.acme.transport.linehaul.capacity.plan.classifyPrimaryCapacityScore

fun summarizeLenientCapacityRollupsForRule(items: MutableList<LenientCapacityRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveLenientLanePlanRulesFromRollup(items: MutableList<LenientCapacityRollup>): MutableList<LenientLanePlanRule> {
    val out = mutableListOf<LenientLanePlanRule>()
    for (item in items) {
        val derived = newLenientLanePlanRule(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankLenientLanePlanRule(item: LenientLanePlanRule): String {
    return classifyPrimaryCapacityScore(item.weightedTotal())
}
