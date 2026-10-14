package com.acme.transport.emissions.carbonreport.tieredreportscore

import com.acme.transport.emissions.fuelburn.claim.BatchedFuelBurnForecast
import com.acme.transport.emissions.fuelburn.claim.lenientFuelBurnAssignmentBucket

fun summarizeBatchedFuelBurnForecastsForScore(items: MutableList<BatchedFuelBurnForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveWeightedCarbonReportScoresFromForecast(items: MutableList<BatchedFuelBurnForecast>): MutableList<WeightedCarbonReportScore> {
    val out = mutableListOf<WeightedCarbonReportScore>()
    for (item in items) {
        val derived = newWeightedCarbonReportScore(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyWeightedCarbonReportScoreExtra(item: WeightedCarbonReportScore): String {
    return lenientFuelBurnAssignmentBucket(item.effectiveVolume())
}
