package com.acme.transport.emissions.fuelburn.adjustment

import com.acme.transport.emissions.carbonreport.tier.WeightedCarbonReportTier
import com.acme.transport.emissions.carbonreport.tier.weightedCarbonReportTierWeight

fun summarizeWeightedCarbonReportTiersForAdjustment(items: MutableList<WeightedCarbonReportTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAdaptiveFuelBurnAdjustmentsFromTier(items: MutableList<WeightedCarbonReportTier>): MutableList<AdaptiveFuelBurnAdjustment> {
    val out = mutableListOf<AdaptiveFuelBurnAdjustment>()
    for (item in items) {
        val derived = newAdaptiveFuelBurnAdjustment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignAdaptiveFuelBurnAdjustment(item: AdaptiveFuelBurnAdjustment): Double {
    return weightedCarbonReportTierWeight(8, true)
}
