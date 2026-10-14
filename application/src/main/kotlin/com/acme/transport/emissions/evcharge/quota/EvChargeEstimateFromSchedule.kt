package com.acme.transport.emissions.evcharge.quota

import com.acme.transport.emissions.routeoffset.entry.RouteOffsetSchedule

fun summarizeRouteOffsetSchedulesForEstimate(items: MutableList<RouteOffsetSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveEvChargeEstimatesFromSchedule(items: MutableList<RouteOffsetSchedule>): MutableList<EvChargeEstimate> {
    val out = mutableListOf<EvChargeEstimate>()
    for (item in items) {
        val derived = newEvChargeEstimate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}
