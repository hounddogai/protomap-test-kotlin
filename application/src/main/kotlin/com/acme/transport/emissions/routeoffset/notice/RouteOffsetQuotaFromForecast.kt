package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.emissions.idling.baseline.RegionalIdlingForecast
import com.acme.transport.emissions.idling.baseline.classifyIdlingCycle

fun summarizeRegionalIdlingForecastsForQuota(items: MutableList<RegionalIdlingForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRouteOffsetQuotasFromForecast(items: MutableList<RegionalIdlingForecast>): MutableList<RouteOffsetQuota> {
    val out = mutableListOf<RouteOffsetQuota>()
    for (item in items) {
        val derived = newRouteOffsetQuota(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun applyRouteOffsetQuota(item: RouteOffsetQuota): String = classifyIdlingCycle(item.projectedValue())
