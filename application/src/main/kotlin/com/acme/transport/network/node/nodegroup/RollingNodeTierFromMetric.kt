package com.acme.transport.network.node.nodegroup

import com.acme.transport.network.node.metric.FallbackNodeMetric
import com.acme.transport.network.node.metric.classifyFallbackNodeMetric

fun summarizeFallbackNodeMetricsForTier(items: MutableList<FallbackNodeMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveRollingNodeTiersFromMetric(items: MutableList<FallbackNodeMetric>): MutableList<RollingNodeTier> {
    val out = mutableListOf<RollingNodeTier>()
    for (item in items) {
        val derived = newRollingNodeTier(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeRollingNodeTier(item: RollingNodeTier): String = classifyFallbackNodeMetric(item.netLoad())
