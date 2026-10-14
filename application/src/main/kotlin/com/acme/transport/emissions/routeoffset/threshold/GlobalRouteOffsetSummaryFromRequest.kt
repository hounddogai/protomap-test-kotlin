package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.emissions.evcharge.baseline.EvChargeRequest
import com.acme.transport.emissions.evcharge.baseline.weightedEvChargeGroupWeight

fun summarizeEvChargeRequestsForSummary(items: MutableList<EvChargeRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveGlobalRouteOffsetSummariesFromRequest(items: MutableList<EvChargeRequest>): MutableList<GlobalRouteOffsetSummary> {
    val out = mutableListOf<GlobalRouteOffsetSummary>()
    for (item in items) {
        val derived = newGlobalRouteOffsetSummary(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankGlobalRouteOffsetSummary(item: GlobalRouteOffsetSummary): Double = weightedEvChargeGroupWeight(6, true)
