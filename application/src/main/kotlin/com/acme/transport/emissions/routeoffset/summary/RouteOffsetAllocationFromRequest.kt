package com.acme.transport.emissions.routeoffset.summary

import com.acme.transport.emissions.routeoffset.rollup.RouteOffsetRequest
import com.acme.transport.emissions.routeoffset.rollup.routeOffsetProfileWeight

fun summarizeRouteOffsetRequestsForAllocation(items: MutableList<RouteOffsetRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveRouteOffsetAllocationsFromRequest(items: MutableList<RouteOffsetRequest>): MutableList<RouteOffsetAllocation> {
    val out = mutableListOf<RouteOffsetAllocation>()
    for (item in items) {
        val derived = newRouteOffsetAllocation(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun allocateRouteOffsetAllocation(item: RouteOffsetAllocation): Double = routeOffsetProfileWeight(1, true)
