package com.acme.transport.network.servicearea.draft

import com.acme.transport.network.node.metric.FallbackNodeMetric
import com.acme.transport.network.node.metric.normalizeNodeAuditCode

fun summarizeFallbackNodeMetricsForAdjustment(items: MutableList<FallbackNodeMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveFallbackServiceAreaAdjustmentsFromMetric(items: MutableList<FallbackNodeMetric>): MutableList<FallbackServiceAreaAdjustment> {
    val out = mutableListOf<FallbackServiceAreaAdjustment>()
    for (item in items) {
        val derived = newFallbackServiceAreaAdjustment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeFallbackServiceAreaAdjustment(item: FallbackServiceAreaAdjustment): String {
    return normalizeNodeAuditCode(item.code)
}
