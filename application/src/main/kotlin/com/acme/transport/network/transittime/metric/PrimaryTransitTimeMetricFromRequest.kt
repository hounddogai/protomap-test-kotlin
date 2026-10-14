package com.acme.transport.network.transittime.metric

import com.acme.transport.network.servicearea.request.ServiceAreaRequest
import com.acme.transport.network.servicearea.request.adaptiveServiceAreaCheckpointBucket

fun summarizeServiceAreaRequestsForMetric(items: MutableList<ServiceAreaRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePrimaryTransitTimeMetricsFromRequest(items: MutableList<ServiceAreaRequest>): MutableList<PrimaryTransitTimeMetric> {
    val out = mutableListOf<PrimaryTransitTimeMetric>()
    for (item in items) {
        val derived = newPrimaryTransitTimeMetric(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizePrimaryTransitTimeMetric(item: PrimaryTransitTimeMetric): String {
    return adaptiveServiceAreaCheckpointBucket(item.measuredValue())
}
