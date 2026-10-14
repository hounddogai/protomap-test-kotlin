package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.network.transittime.baseline.WeightedTransitTimeSnapshot
import com.acme.transport.network.transittime.baseline.weightedTransitTimeSnapshotBucket

fun summarizeWeightedTransitTimeSnapshotsForForecast(items: MutableList<WeightedTransitTimeSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveFallbackServiceAreaForecastsFromSnapshot(items: MutableList<WeightedTransitTimeSnapshot>): MutableList<FallbackServiceAreaForecast> {
    val out = mutableListOf<FallbackServiceAreaForecast>()
    for (item in items) {
        val derived = newFallbackServiceAreaForecast(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planFallbackServiceAreaForecast(item: FallbackServiceAreaForecast): String {
    return weightedTransitTimeSnapshotBucket(item.adjustedExposure())
}
