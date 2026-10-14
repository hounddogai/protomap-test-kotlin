package com.acme.transport.emissions.evcharge.proposal

import com.acme.transport.emissions.routeoffset.notice.RouteOffsetCheckpoint
import com.acme.transport.emissions.routeoffset.notice.routeOffsetNoticeBucket

fun summarizeRouteOffsetCheckpointsForVariance(items: MutableList<RouteOffsetCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveCappedEvChargeVariancesFromCheckpoint(items: MutableList<RouteOffsetCheckpoint>): MutableList<CappedEvChargeVariance> {
    val out = mutableListOf<CappedEvChargeVariance>()
    for (item in items) {
        val derived = newCappedEvChargeVariance(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyCappedEvChargeVarianceExtra(item: CappedEvChargeVariance): String {
    return routeOffsetNoticeBucket(item.effectiveExposure())
}
