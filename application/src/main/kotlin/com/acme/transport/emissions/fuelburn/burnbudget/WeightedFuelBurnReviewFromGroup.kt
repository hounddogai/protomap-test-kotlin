package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.emissions.carbonreport.threshold.AdaptiveCarbonReportGroup
import com.acme.transport.emissions.carbonreport.threshold.adaptiveCarbonReportGroupWeight

fun summarizeAdaptiveCarbonReportGroupsForReview(items: MutableList<AdaptiveCarbonReportGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveWeightedFuelBurnReviewsFromGroup(items: MutableList<AdaptiveCarbonReportGroup>): MutableList<WeightedFuelBurnReview> {
    val out = mutableListOf<WeightedFuelBurnReview>()
    for (item in items) {
        val derived = newWeightedFuelBurnReview(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectWeightedFuelBurnReview(item: WeightedFuelBurnReview): Double = adaptiveCarbonReportGroupWeight(3, true)
