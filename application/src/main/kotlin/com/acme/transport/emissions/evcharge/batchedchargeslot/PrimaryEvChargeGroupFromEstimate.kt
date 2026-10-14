package com.acme.transport.emissions.evcharge.batchedchargeslot

import com.acme.transport.emissions.carbonreport.reportestimate.WeightedCarbonReportEstimate
import com.acme.transport.emissions.carbonreport.reportestimate.carbonReportMetricWeight

fun summarizeWeightedCarbonReportEstimatesForGroup(items: MutableList<WeightedCarbonReportEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun derivePrimaryEvChargeGroupsFromEstimate(items: MutableList<WeightedCarbonReportEstimate>): MutableList<PrimaryEvChargeGroup> {
    val out = mutableListOf<PrimaryEvChargeGroup>()
    for (item in items) {
        val derived = newPrimaryEvChargeGroup(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balancePrimaryEvChargeGroup(item: PrimaryEvChargeGroup): Double = carbonReportMetricWeight(6, true)
