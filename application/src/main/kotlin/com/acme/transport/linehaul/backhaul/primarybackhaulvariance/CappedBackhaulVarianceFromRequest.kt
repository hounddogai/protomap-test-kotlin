package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.linehaul.backhaul.template.FallbackBackhaulRequest
import com.acme.transport.linehaul.backhaul.template.fallbackBackhaulRequestBucket

fun summarizeFallbackBackhaulRequestsForVariance(items: MutableList<FallbackBackhaulRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveCappedBackhaulVariancesFromRequest(items: MutableList<FallbackBackhaulRequest>): MutableList<CappedBackhaulVariance> {
    val out = mutableListOf<CappedBackhaulVariance>()
    for (item in items) {
        val derived = newCappedBackhaulVariance(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveCappedBackhaulVariance(item: CappedBackhaulVariance): String {
    return fallbackBackhaulRequestBucket(item.weightedValue())
}
