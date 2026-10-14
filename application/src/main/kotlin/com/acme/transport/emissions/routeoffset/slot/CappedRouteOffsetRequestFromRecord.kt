package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.emissions.idling.idlingmetric.IdlingRecord
import com.acme.transport.emissions.idling.idlingmetric.legacyIdlingMetricBucket

fun summarizeIdlingRecordsForRequest(items: MutableList<IdlingRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveCappedRouteOffsetRequestsFromRecord(items: MutableList<IdlingRecord>): MutableList<CappedRouteOffsetRequest> {
    val out = mutableListOf<CappedRouteOffsetRequest>()
    for (item in items) {
        val derived = newCappedRouteOffsetRequest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeCappedRouteOffsetRequest(item: CappedRouteOffsetRequest): String {
    return legacyIdlingMetricBucket(item.weightedLoad())
}
