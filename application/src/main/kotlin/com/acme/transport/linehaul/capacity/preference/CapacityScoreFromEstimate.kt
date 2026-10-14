package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.linehaul.laneplan.planbudget.SeasonalLanePlanEstimate
import com.acme.transport.linehaul.laneplan.planbudget.adaptiveLanePlanTemplateWeight

fun summarizeSeasonalLanePlanEstimatesForScore(items: MutableList<SeasonalLanePlanEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveCapacityScoresFromEstimate(items: MutableList<SeasonalLanePlanEstimate>): MutableList<CapacityScore> {
    val out = mutableListOf<CapacityScore>()
    for (item in items) {
        val derived = newCapacityScore(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankCapacityScoreExtra(item: CapacityScore): Double = adaptiveLanePlanTemplateWeight(7, true)
