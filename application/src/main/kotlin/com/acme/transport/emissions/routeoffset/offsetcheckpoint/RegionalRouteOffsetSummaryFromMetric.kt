package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.emissions.fuelburn.burnnotice.FuelBurnMetric
import com.acme.transport.emissions.fuelburn.burnnotice.normalizeFuelBurnMetricCode

fun summarizeFuelBurnMetricsForSummary(items: MutableList<FuelBurnMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveRegionalRouteOffsetSummariesFromMetric(items: MutableList<FuelBurnMetric>): MutableList<RegionalRouteOffsetSummary> {
    val out = mutableListOf<RegionalRouteOffsetSummary>()
    for (item in items) {
        val derived = newRegionalRouteOffsetSummary(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectRegionalRouteOffsetSummary(item: RegionalRouteOffsetSummary): String {
    return normalizeFuelBurnMetricCode(item.code)
}
