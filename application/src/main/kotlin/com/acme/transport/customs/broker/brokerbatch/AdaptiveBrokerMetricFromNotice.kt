package com.acme.transport.customs.broker.brokerbatch

import com.acme.transport.customs.clearance.request.DefaultClearanceNotice
import com.acme.transport.customs.clearance.request.clearanceRequestBucket

fun summarizeDefaultClearanceNoticesForMetric(items: MutableList<DefaultClearanceNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveAdaptiveBrokerMetricsFromNotice(items: MutableList<DefaultClearanceNotice>): MutableList<AdaptiveBrokerMetric> {
    val out = mutableListOf<AdaptiveBrokerMetric>()
    for (item in items) {
        val derived = newAdaptiveBrokerMetric(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveAdaptiveBrokerMetric(item: AdaptiveBrokerMetric): String = clearanceRequestBucket(item.measuredValue())
