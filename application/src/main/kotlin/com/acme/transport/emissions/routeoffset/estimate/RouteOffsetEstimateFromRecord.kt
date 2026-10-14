package com.acme.transport.emissions.routeoffset.estimate

import com.acme.transport.emissions.evcharge.manifest.FallbackEvChargeRecord
import com.acme.transport.emissions.evcharge.manifest.fallbackEvChargeRecordWeight

fun summarizeFallbackEvChargeRecordsForEstimate(items: MutableList<FallbackEvChargeRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRouteOffsetEstimatesFromRecord(items: MutableList<FallbackEvChargeRecord>): MutableList<RouteOffsetEstimate> {
    val out = mutableListOf<RouteOffsetEstimate>()
    for (item in items) {
        val derived = newRouteOffsetEstimate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileRouteOffsetEstimate(item: RouteOffsetEstimate): Double = fallbackEvChargeRecordWeight(3, true)
