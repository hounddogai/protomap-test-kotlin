package com.acme.transport.network.node.metric

import com.acme.transport.network.zone.pendingzonemapping.PendingZoneMapping
import com.acme.transport.network.zone.pendingzonemapping.normalizeStrictZoneRecordCode

fun summarizePendingZoneMappingsForMetric(items: MutableList<PendingZoneMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveFallbackNodeMetricsFromMapping(items: MutableList<PendingZoneMapping>): MutableList<FallbackNodeMetric> {
    val out = mutableListOf<FallbackNodeMetric>()
    for (item in items) {
        val derived = newFallbackNodeMetric(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignFallbackNodeMetric(item: FallbackNodeMetric): String = normalizeStrictZoneRecordCode(item.code)
