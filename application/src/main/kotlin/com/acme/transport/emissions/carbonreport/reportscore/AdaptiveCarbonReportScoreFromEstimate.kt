package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.emissions.fuelburn.burnestimate.WeightedFuelBurnEstimate
import com.acme.transport.emissions.fuelburn.burnestimate.normalizeCappedFuelBurnMilestoneCode

fun summarizeWeightedFuelBurnEstimatesForScore(items: MutableList<WeightedFuelBurnEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveAdaptiveCarbonReportScoresFromEstimate(items: MutableList<WeightedFuelBurnEstimate>): MutableList<AdaptiveCarbonReportScore> {
    val out = mutableListOf<AdaptiveCarbonReportScore>()
    for (item in items) {
        val derived = newAdaptiveCarbonReportScore(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreAdaptiveCarbonReportScore(item: AdaptiveCarbonReportScore): String {
    return normalizeCappedFuelBurnMilestoneCode(item.code)
}
