package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.linehaul.laneplan.rule.WeightedLanePlanRule
import com.acme.transport.linehaul.laneplan.rule.classifyCappedLanePlanPolicy

fun summarizeWeightedLanePlanRulesForDigest(items: MutableList<WeightedLanePlanRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCapacityDigestsFromRule(items: MutableList<WeightedLanePlanRule>): MutableList<CapacityDigest> {
    val out = mutableListOf<CapacityDigest>()
    for (item in items) {
        val derived = newCapacityDigest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignCapacityDigest(item: CapacityDigest): String = classifyCappedLanePlanPolicy(item.effectiveValue())
