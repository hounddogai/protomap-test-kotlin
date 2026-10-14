package com.acme.transport.terminal.traileryard.entry

import com.acme.transport.terminal.crossdock.snapshot.CrossDockQuota
import com.acme.transport.terminal.crossdock.snapshot.defaultCrossDockSnapshotBucket

fun summarizeCrossDockQuotasForVariance(items: MutableList<CrossDockQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveSeasonalTrailerYardVariancesFromQuota(items: MutableList<CrossDockQuota>): MutableList<SeasonalTrailerYardVariance> {
    val out = mutableListOf<SeasonalTrailerYardVariance>()
    for (item in items) {
        val derived = newSeasonalTrailerYardVariance(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifySeasonalTrailerYardVariance(item: SeasonalTrailerYardVariance): String {
    return defaultCrossDockSnapshotBucket(item.projectedLoad())
}
