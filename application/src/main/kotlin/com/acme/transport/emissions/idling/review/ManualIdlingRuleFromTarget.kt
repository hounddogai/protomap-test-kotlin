package com.acme.transport.emissions.idling.review

import com.acme.transport.emissions.fuelburn.adjustment.RegionalFuelBurnTarget
import com.acme.transport.emissions.fuelburn.adjustment.classifyAdaptiveFuelBurnAdjustment

fun summarizeRegionalFuelBurnTargetsForRule(items: MutableList<RegionalFuelBurnTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveManualIdlingRulesFromTarget(items: MutableList<RegionalFuelBurnTarget>): MutableList<ManualIdlingRule> {
    val out = mutableListOf<ManualIdlingRule>()
    for (item in items) {
        val derived = newManualIdlingRule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeManualIdlingRule(item: ManualIdlingRule): String {
    return classifyAdaptiveFuelBurnAdjustment(item.measuredValue())
}
