package com.acme.transport.terminal.dockdoor.profile

import com.acme.transport.terminal.traileryard.plan.TieredTrailerYardMetric
import com.acme.transport.terminal.traileryard.plan.fallbackTrailerYardBundleBucket

fun summarizeTieredTrailerYardMetricsForBatch(items: MutableList<TieredTrailerYardMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveStrictDockDoorBatchesFromMetric(items: MutableList<TieredTrailerYardMetric>): MutableList<StrictDockDoorBatch> {
    val out = mutableListOf<StrictDockDoorBatch>()
    for (item in items) {
        val derived = newStrictDockDoorBatch(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeStrictDockDoorBatch(item: StrictDockDoorBatch): String {
    return fallbackTrailerYardBundleBucket(item.measuredValue())
}
