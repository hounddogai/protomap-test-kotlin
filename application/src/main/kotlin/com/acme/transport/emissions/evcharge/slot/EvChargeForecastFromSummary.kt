package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.emissions.routeoffset.summary.RouteOffsetSummary
import com.acme.transport.emissions.routeoffset.summary.normalizeRouteOffsetBaselineCode

fun summarizeRouteOffsetSummariesForForecast(items: MutableList<RouteOffsetSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveEvChargeForecastsFromSummary(items: MutableList<RouteOffsetSummary>): MutableList<EvChargeForecast> {
    val out = mutableListOf<EvChargeForecast>()
    for (item in items) {
        val derived = newEvChargeForecast(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignEvChargeForecast(item: EvChargeForecast): String = normalizeRouteOffsetBaselineCode(item.code)
