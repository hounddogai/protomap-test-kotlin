package com.acme.transport.emissions.idling.bundle

import com.acme.transport.emissions.carbonreport.tier.StrictCarbonReportMetric
import com.acme.transport.emissions.carbonreport.tier.weightedCarbonReportTierWeight

fun summarizeStrictCarbonReportMetricsForWindow(items: MutableList<StrictCarbonReportMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveCappedIdlingWindowsFromMetric(items: MutableList<StrictCarbonReportMetric>): MutableList<CappedIdlingWindow> {
    val out = mutableListOf<CappedIdlingWindow>()
    for (item in items) {
        val derived = newCappedIdlingWindow(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun settleCappedIdlingWindow(item: CappedIdlingWindow): Double = weightedCarbonReportTierWeight(2, true)
