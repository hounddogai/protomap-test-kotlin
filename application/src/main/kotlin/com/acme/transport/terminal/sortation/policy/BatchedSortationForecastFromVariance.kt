package com.acme.transport.terminal.sortation.policy

import com.acme.transport.terminal.sortation.estimate.FallbackSortationVariance
import com.acme.transport.terminal.sortation.estimate.fallbackSortationVarianceWeight

fun summarizeFallbackSortationVariancesForForecast(items: MutableList<FallbackSortationVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveBatchedSortationForecastsFromVariance(items: MutableList<FallbackSortationVariance>): MutableList<BatchedSortationForecast> {
    val out = mutableListOf<BatchedSortationForecast>()
    for (item in items) {
        val derived = newBatchedSortationForecast(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreBatchedSortationForecast(item: BatchedSortationForecast): Double {
    return fallbackSortationVarianceWeight(5, true)
}
