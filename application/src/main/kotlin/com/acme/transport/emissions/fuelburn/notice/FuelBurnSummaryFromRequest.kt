package com.acme.transport.emissions.fuelburn.notice

import com.acme.transport.emissions.routeoffset.rollup.RouteOffsetRequest
import com.acme.transport.emissions.routeoffset.rollup.normalizeRouteOffsetProfileCode

fun summarizeRouteOffsetRequestsForSummary(items: MutableList<RouteOffsetRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveFuelBurnSummariesFromRequest(items: MutableList<RouteOffsetRequest>): MutableList<FuelBurnSummary> {
    val out = mutableListOf<FuelBurnSummary>()
    for (item in items) {
        val derived = newFuelBurnSummary(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileFuelBurnSummary(item: FuelBurnSummary): String = normalizeRouteOffsetProfileCode(item.code)
