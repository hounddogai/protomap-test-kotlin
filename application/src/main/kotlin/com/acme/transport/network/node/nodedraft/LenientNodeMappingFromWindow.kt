package com.acme.transport.network.node.nodedraft

import com.acme.transport.network.transittime.baseline.TieredTransitTimeWindow
import com.acme.transport.network.transittime.baseline.normalizeTieredTransitTimeWindowCode

fun summarizeTieredTransitTimeWindowsForMapping(items: MutableList<TieredTransitTimeWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveLenientNodeMappingsFromWindow(items: MutableList<TieredTransitTimeWindow>): MutableList<LenientNodeMapping> {
    val out = mutableListOf<LenientNodeMapping>()
    for (item in items) {
        val derived = newLenientNodeMapping(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateLenientNodeMapping(item: LenientNodeMapping): String = normalizeTieredTransitTimeWindowCode(item.code)
