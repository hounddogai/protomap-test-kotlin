package com.acme.transport.emissions.idling.idlingforecast

import com.acme.transport.emissions.routeoffset.forecast.RouteOffsetForecast
import com.acme.transport.emissions.routeoffset.forecast.batchedRouteOffsetAllocationBucket

fun summarizeRouteOffsetForecastsForProfile(items: MutableList<RouteOffsetForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveIdlingProfilesFromForecast(items: MutableList<RouteOffsetForecast>): MutableList<IdlingProfile> {
    val out = mutableListOf<IdlingProfile>()
    for (item in items) {
        val derived = newIdlingProfile(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectIdlingProfile(item: IdlingProfile): String = batchedRouteOffsetAllocationBucket(item.projectedValue())
