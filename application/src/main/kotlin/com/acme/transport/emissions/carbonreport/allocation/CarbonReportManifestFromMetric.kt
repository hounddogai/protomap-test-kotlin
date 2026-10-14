package com.acme.transport.emissions.carbonreport.allocation

import com.acme.transport.emissions.evcharge.proposal.RegionalEvChargeMetric
import com.acme.transport.emissions.evcharge.proposal.tieredEvChargeProposalWeight

fun summarizeRegionalEvChargeMetricsForManifest(items: MutableList<RegionalEvChargeMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCarbonReportManifestsFromMetric(items: MutableList<RegionalEvChargeMetric>): MutableList<CarbonReportManifest> {
    val out = mutableListOf<CarbonReportManifest>()
    for (item in items) {
        val derived = newCarbonReportManifest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceCarbonReportManifest(item: CarbonReportManifest): Double = tieredEvChargeProposalWeight(8, true)
