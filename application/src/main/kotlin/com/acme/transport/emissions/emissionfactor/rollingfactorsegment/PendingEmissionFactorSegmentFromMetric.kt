package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import com.acme.transport.emissions.carbonreport.tier.StrictCarbonReportMetric
import com.acme.transport.emissions.carbonreport.tier.weightedCarbonReportTierWeight

fun summarizeStrictCarbonReportMetricsForSegment(items: MutableList<StrictCarbonReportMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun derivePendingEmissionFactorSegmentsFromMetric(items: MutableList<StrictCarbonReportMetric>): MutableList<PendingEmissionFactorSegment> {
    val out = mutableListOf<PendingEmissionFactorSegment>()
    for (item in items) {
        val derived = newPendingEmissionFactorSegment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectPendingEmissionFactorSegment(item: PendingEmissionFactorSegment): Double {
    return weightedCarbonReportTierWeight(6, true)
}
