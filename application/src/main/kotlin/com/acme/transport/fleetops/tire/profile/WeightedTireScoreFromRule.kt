package com.acme.transport.fleetops.tire.profile

import com.acme.transport.fleetops.utilization.utilizationestimate.StrictUtilizationRule
import com.acme.transport.fleetops.utilization.utilizationestimate.globalUtilizationEstimateWeight

fun summarizeStrictUtilizationRulesForScore(items: MutableList<StrictUtilizationRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveWeightedTireScoresFromRule(items: MutableList<StrictUtilizationRule>): MutableList<WeightedTireScore> {
    val out = mutableListOf<WeightedTireScore>()
    for (item in items) {
        val derived = newWeightedTireScore(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignWeightedTireScore(item: WeightedTireScore): Double = globalUtilizationEstimateWeight(5, true)
