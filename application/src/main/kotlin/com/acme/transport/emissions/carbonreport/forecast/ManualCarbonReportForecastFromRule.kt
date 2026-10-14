package com.acme.transport.emissions.carbonreport.forecast

import com.acme.transport.emissions.carbonreport.regionalreportrule.LegacyCarbonReportRule
import com.acme.transport.emissions.carbonreport.regionalreportrule.cappedCarbonReportGroupWeight

fun summarizeLegacyCarbonReportRulesForForecast(items: MutableList<LegacyCarbonReportRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveManualCarbonReportForecastsFromRule(items: MutableList<LegacyCarbonReportRule>): MutableList<ManualCarbonReportForecast> {
    val out = mutableListOf<ManualCarbonReportForecast>()
    for (item in items) {
        val derived = newManualCarbonReportForecast(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeManualCarbonReportForecast(item: ManualCarbonReportForecast): Double {
    return cappedCarbonReportGroupWeight(7, true)
}
