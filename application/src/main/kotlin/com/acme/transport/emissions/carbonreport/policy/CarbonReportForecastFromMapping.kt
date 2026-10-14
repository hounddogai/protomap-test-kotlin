package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.emissions.routeoffset.mapping.RegionalRouteOffsetMapping
import com.acme.transport.emissions.routeoffset.mapping.classifyFallbackRouteOffsetProfile

fun summarizeRegionalRouteOffsetMappingsForForecast(items: MutableList<RegionalRouteOffsetMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveCarbonReportForecastsFromMapping(items: MutableList<RegionalRouteOffsetMapping>): MutableList<CarbonReportForecast> {
    val out = mutableListOf<CarbonReportForecast>()
    for (item in items) {
        val derived = newCarbonReportForecast(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchCarbonReportForecast(item: CarbonReportForecast): String {
    return classifyFallbackRouteOffsetProfile(item.measuredValue())
}
