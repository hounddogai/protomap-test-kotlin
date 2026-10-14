package com.acme.transport.emissions.fuelburn.metric

import com.acme.transport.emissions.carbonreport.tieredreportscore.SeasonalCarbonReportEstimate
import com.acme.transport.emissions.carbonreport.tieredreportscore.weightedCarbonReportScoreBucket

fun summarizeSeasonalCarbonReportEstimatesForSlot(items: MutableList<SeasonalCarbonReportEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveTieredFuelBurnSlotsFromEstimate(items: MutableList<SeasonalCarbonReportEstimate>): MutableList<TieredFuelBurnSlot> {
    val out = mutableListOf<TieredFuelBurnSlot>()
    for (item in items) {
        val derived = newTieredFuelBurnSlot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectTieredFuelBurnSlot(item: TieredFuelBurnSlot): String {
    return weightedCarbonReportScoreBucket(item.measuredValue())
}
